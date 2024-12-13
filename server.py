from flask import Flask, request, jsonify
from werkzeug.security import generate_password_hash, check_password_hash
from flask_cors import CORS
import sqlite3

app = Flask(__name__)
CORS(app)

# Helper function to query the database
def query_db(query, args=(), one=False):
    with sqlite3.connect('bank.db') as conn:
        conn.row_factory = sqlite3.Row
        cursor = conn.cursor()
        cursor.execute(query, args)
        result = cursor.fetchall()
        return (result[0] if result else None) if one else result

# Helper function to update balance and record transactions
def update_balance(user_id, amount, operation, note=''):
    try:
        with sqlite3.connect('bank.db') as conn:
            cursor = conn.cursor()

            # Verify user exists and fetch current balance
            user = query_db('SELECT balance, is_frozen FROM users WHERE id = ?', (user_id,), one=True)
            if not user:
                return {'success': False, 'error': 'User not found'}
            if user['is_frozen']:
                return {'success': False, 'error': 'Account is frozen'}

            # Calculate new balance
            new_balance = user['balance'] + amount if operation == 'DEPOSIT' else user['balance'] - amount
            if new_balance < 0:
                return {'success': False, 'error': 'Insufficient funds'}

            # Update balance and record transaction
            cursor.execute('UPDATE users SET balance = ? WHERE id = ?', (new_balance, user_id))
            cursor.execute(
                '''
                INSERT INTO transactions (user_id, type, amount, note) 
                VALUES (?, ?, ?, ?)
                ''', 
                (user_id, operation, abs(amount), note)
            )
            conn.commit()
            return {'success': True, 'new_balance': new_balance}
    except sqlite3.Error as e:
        return {'success': False, 'error': f'Database error: {str(e)}'}

# Login
@app.route('/login', methods=['POST'])
def login():
    try:
        data = request.json
        if not data or 'username' not in data or 'password' not in data:
            return jsonify({'success': False, 'error': 'Missing username or password'}), 400

        user = query_db('SELECT * FROM users WHERE username = ?', (data['username'],), one=True)
        if user and check_password_hash(user['password'], data['password']):
            return jsonify({'success': True, 'data': {'id': user['id'], 'role': user['role'], 'is_frozen': user['is_frozen']}}), 200
        return jsonify({'success': False, 'error': 'Invalid credentials'}), 401
    except Exception as e:
        return jsonify({'success': False, 'error': f'Unexpected error: {str(e)}'}), 500

# Get balance
@app.route('/member/balance', methods=['GET'])
def get_balance():
    user_id = request.args.get('user_id')
    user = query_db('SELECT balance FROM users WHERE id = ? AND is_frozen = 0', (user_id,), one=True)
    if user:
        return jsonify({'success': True, 'data': {'balance': user['balance']}}), 200
    return jsonify({'success': False, 'error': 'User not found or account frozen'}), 404

# Create user (Admin)
@app.route('/admin/create_user', methods=['POST'])
def create_user():
    data = request.json
    username = data.get('username')
    password = data.get('password')
    role = data.get('role')
    balance = data.get('balance', 0)

    if not username or not password or not role:
        return jsonify({'success': False, 'error': 'Missing required fields'}), 400
    if role not in ['MEMBER', 'STAFF', 'ADMIN']:
        return jsonify({'success': False, 'error': 'Invalid role'}), 400

    try:
        balance = float(balance)
        if balance < 0:
            return jsonify({'success': False, 'error': 'Balance cannot be negative'}), 400
    except ValueError:
        return jsonify({'success': False, 'error': 'Invalid balance value'}), 400

    hashed_password = generate_password_hash(password)
    try:
        with sqlite3.connect('bank.db') as conn:
            cursor = conn.cursor()
            cursor.execute(
                'INSERT INTO users (username, password, role, balance) VALUES (?, ?, ?, ?)',
                (username, hashed_password, role, balance)
            )
            conn.commit()
            return jsonify({'success': True, 'message': 'User created successfully!'}), 201
    except sqlite3.IntegrityError:
        return jsonify({'success': False, 'error': 'Username already exists'}), 409

# Withdraw
@app.route('/member/withdraw', methods=['POST'])
def withdraw():
    data = request.json
    user_id = data.get('user_id')
    amount = data.get('amount')
    password = data.get('password')
    note = data.get('note', '')  # Optional note

    try:
        amount = float(amount)
        if amount <= 0:
            return jsonify({'success': False, 'error': 'Amount must be greater than zero'}), 400
    except ValueError:
        return jsonify({'success': False, 'error': 'Invalid amount'}), 400

    user = query_db('SELECT * FROM users WHERE id = ?', (user_id,), one=True)
    if not user or not check_password_hash(user['password'], password):
        return jsonify({'success': False, 'error': 'Invalid user or password'}), 401

    result = update_balance(user_id, -amount, 'WITHDRAW', note)
    if result['success']:
        return jsonify({'success': True, 'new_balance': result['new_balance']}), 200
    return jsonify({'success': False, 'error': result['error']}), 400

@app.route('/member/deposit', methods=['POST'])
def deposit():
    data = request.json
    user_id = data.get('user_id')
    amount = data.get('amount')
    password = data.get('password')
    note = data.get('note', '')  # Optional note

    try:
        amount = float(amount)
        if amount <= 0:
            return jsonify({'success': False, 'error': 'Amount must be greater than zero'}), 400
    except ValueError:
        return jsonify({'success': False, 'error': 'Invalid amount'}), 400

    user = query_db('SELECT * FROM users WHERE id = ?', (user_id,), one=True)
    if not user or not check_password_hash(user['password'], password):
        return jsonify({'success': False, 'error': 'Invalid user or password'}), 401

    result = update_balance(user_id, amount, 'DEPOSIT', note)
    if result['success']:
        return jsonify({'success': True, 'new_balance': result['new_balance']}), 200
    return jsonify({'success': False, 'error': result['error']}), 400

# Transfer
@app.route('/member/transfer', methods=['POST'])
def transfer_money():
    data = request.json
    user_id = data.get('user_id')
    recipient_id = data.get('recipient_id')
    amount = data.get('amount')
    password = data.get('password')
    note = data.get('note', '')  # Optional note

    try:
        amount = float(amount)
        if amount <= 0:
            return jsonify({'success': False, 'error': 'Amount must be greater than zero'}), 400
    except ValueError:
        return jsonify({'success': False, 'error': 'Invalid amount'}), 400

    with sqlite3.connect('bank.db') as conn:
        cursor = conn.cursor()
        sender = query_db('SELECT password, balance FROM users WHERE id = ?', (user_id,), one=True)
        recipient = query_db('SELECT id FROM users WHERE id = ?', (recipient_id,), one=True)

        if not sender:
            return jsonify({'success': False, 'error': 'Sender not found'}), 404
        if not check_password_hash(sender['password'], password):
            return jsonify({'success': False, 'error': 'Incorrect password'}), 401
        if sender['balance'] < amount:
            return jsonify({'success': False, 'error': 'Insufficient funds'}), 400
        if not recipient:
            return jsonify({'success': False, 'error': 'Recipient not found'}), 404

        # Deduct from sender and add to recipient
        cursor.execute('UPDATE users SET balance = balance - ? WHERE id = ?', (amount, user_id))
        cursor.execute('UPDATE users SET balance = balance + ? WHERE id = ?', (amount, recipient_id))

        # Record transactions
        cursor.execute(
            '''
            INSERT INTO transactions (user_id, type, amount, note) 
            VALUES (?, 'TRANSFER_OUT', ?, ?)
            ''', 
            (user_id, -amount, note)
        )
        cursor.execute(
            '''
            INSERT INTO transactions (user_id, type, amount, note) 
            VALUES (?, 'TRANSFER_IN', ?, ?)
            ''', 
            (recipient_id, amount, f'Received from user {user_id}')
        )
        conn.commit()
        return jsonify({'success': True, 'message': 'Transfer completed successfully'}), 200

# View transactions
@app.route('/transactions', methods=['GET'])
def view_transactions():
    user_id = request.args.get('user_id')
    if not user_id:
        return jsonify({'success': False, 'error': 'Missing user_id'}), 400

    try:
        # Query the transactions table
        transactions = query_db(
            '''
            SELECT id AS transaction_id, type, amount, note, timestamp 
            FROM transactions 
            WHERE user_id = ? 
            ORDER BY timestamp DESC
            ''', 
            (user_id,)
        )

        if not transactions:
            return jsonify({'success': False, 'error': 'No transaction history found'}), 404

        # Format transactions for response
        transactions_list = [
            {
                'transaction_id': t['transaction_id'],
                'type': t['type'],
                'amount': t['amount'],
                'note': t.get('note', ''),
                'timestamp': t['timestamp']
            } for t in transactions
        ]
        return jsonify({'success': True, 'data': transactions_list}), 200

    except Exception as e:
        return jsonify({'success': False, 'error': f'Error retrieving transactions: {str(e)}'}), 500


# Modify user
@app.route('/admin/modify_user', methods=['POST'])
def modify_user():
    data = request.json
    user_id = data.get('user_id')
    password = data.get('password')
    role = data.get('role')
    balance = data.get('balance')

    if not user_id or not role or balance is None:
        return jsonify({'success': False, 'error': 'Missing user_id, role, or balance'}), 400

    try:
        balance = float(balance)
        if balance < 0:
            return jsonify({'success': False, 'error': 'Balance cannot be negative'}), 400
    except ValueError:
        return jsonify({'success': False, 'error': 'Invalid balance value'}), 400

    try:
        with sqlite3.connect('bank.db') as conn:
            cursor = conn.cursor()
            if password:
                hashed_password = generate_password_hash(password)
                cursor.execute('UPDATE users SET password = ? WHERE id = ?', (hashed_password, user_id))
            cursor.execute('UPDATE users SET role = ?, balance = ? WHERE id = ?', (role, balance, user_id))
            conn.commit()
            return jsonify({'success': True, 'message': 'User updated successfully!'}), 200
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)}), 500

# Get users (Admin)
@app.route('/admin/get_users', methods=['GET'])
def get_users():
    try:
        users = query_db('SELECT id, username, balance, role, is_frozen FROM users')
        users_list = [{'id': user['id'], 'username': user['username'], 'balance': user['balance'], 'role': user['role'], 'is_frozen': user['is_frozen']} for user in users]
        return jsonify({'success': True, 'data': users_list}), 200
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)}), 500

@app.route('/staff/view_balance', methods=['GET'])
def staff_view_balance():
    user_id = request.args.get('user_id')
    if not user_id:
        return jsonify({'success': False, 'error': 'Missing user_id'}), 400

    user = query_db('SELECT id, username, balance FROM users WHERE id = ? AND is_frozen = 0', (user_id,), one=True)
    if not user:
        return jsonify({'success': False, 'error': 'User not found or account is frozen'}), 404

    return jsonify({'success': True, 'data': {'id': user['id'], 'username': user['username'], 'balance': user['balance']}}), 200


@app.route('/staff/view_transaction_history', methods=['GET'])
def staff_view_transaction_history():
    user_id = request.args.get('user_id')
    if not user_id:
        return jsonify({'success': False, 'error': 'Missing user_id'}), 400

    transactions = query_db('SELECT id AS transaction_id, type, amount, timestamp FROM transactions WHERE user_id = ? ORDER BY timestamp DESC', (user_id,))
    if not transactions:
        return jsonify({'success': False, 'error': 'No transaction history available'}), 404

    transactions_list = [{'transaction_id': t['transaction_id'], 'type': t['type'], 'amount': t['amount'], 'timestamp': t['timestamp']} for t in transactions]
    return jsonify({'success': True, 'data': transactions_list}), 200


@app.route('/staff/freeze_account', methods=['POST'])
def staff_freeze_account():
    data = request.json
    user_id = data.get('user_id')
    if not user_id:
        return jsonify({'success': False, 'error': 'Missing user_id'}), 400

    try:
        with sqlite3.connect('bank.db') as conn:
            cursor = conn.cursor()
            cursor.execute('UPDATE users SET is_frozen = 1 WHERE id = ?', (user_id,))
            conn.commit()
        return jsonify({'success': True, 'message': f'Account with user_id {user_id} has been frozen'}), 200
    except sqlite3.Error as e:
        return jsonify({'success': False, 'error': f'Database error: {str(e)}'}), 500

# Main function to run the server
if __name__ == '__main__':
    app.run(debug=True)
