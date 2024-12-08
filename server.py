from flask import Flask, request, jsonify
from werkzeug.security import generate_password_hash, check_password_hash
from flask_cors import CORS
import sqlite3

app = Flask(__name__)
CORS(app)

def query_db(query, args=(), one=False):
    with sqlite3.connect('bank.db') as conn:
        conn.row_factory = sqlite3.Row
        cursor = conn.cursor()
        cursor.execute(query, args)
        result = cursor.fetchall()
        return (result[0] if result else None) if one else result

@app.route('/login', methods=['POST'])
def login():
    data = request.json
    user = query_db('SELECT * FROM users WHERE username = ?', (data['username'],), one=True)
    if user and check_password_hash(user['password'], data['password']):
        return jsonify({'success': True, 'data': {'id': user['id'], 'role': user['role']}}), 200
    return jsonify({'success': False, 'error': 'Invalid credentials'}), 401

@app.route('/member/balance', methods=['GET'])
def get_balance():
    user_id = request.args.get('user_id')
    user = query_db('SELECT balance FROM users WHERE id = ?', (user_id,), one=True)
    if user:
        return jsonify({'success': True, 'data': {'balance': user['balance']}}), 200
    return jsonify({'success': False, 'error': 'User not found'}), 404

@app.route('/admin/create_user', methods=['POST'])
def create_user():
    data = request.json
    if not data.get('username') or not data.get('password') or not data.get('role'):
        return jsonify({'success': False, 'error': 'Missing required fields'}), 400
    if data['role'] not in ['MEMBER', 'STAFF', 'ADMIN']:
        return jsonify({'success': False, 'error': 'Invalid role'}), 400

    hashed_password = generate_password_hash(data['password'])
    try:
        query_db('INSERT INTO users (username, password, role) VALUES (?, ?, ?)',
                 (data['username'], hashed_password, data['role']))
        return jsonify({'success': True, 'message': 'User created successfully'}), 201
    except sqlite3.IntegrityError:
        return jsonify({'success': False, 'error': 'Username already exists'}), 409

@app.route('/member/withdraw', methods=['POST'])
def withdraw():
    data = request.json
    user_id = data.get('user_id')
    amount = data.get('amount')
    password = data.get('password')

    if not user_id or not amount or not password:
        return jsonify({'success': False, 'error': 'Missing required fields'}), 400

    try:
        user = query_db('SELECT * FROM users WHERE id = ?', (user_id,), one=True)
        if not user:
            return jsonify({'success': False, 'error': 'User not found'}), 404

        if not check_password_hash(user['password'], password):
            return jsonify({'success': False, 'error': 'Invalid password'}), 401

        current_balance = user['balance']
        if current_balance < amount:
            return jsonify({'success': False, 'error': 'Insufficient balance'}), 400

        new_balance = current_balance - amount
        query_db('UPDATE users SET balance = ? WHERE id = ?', (new_balance, user_id))

        query_db(
            'INSERT INTO transactions (user_id, type, amount) VALUES (?, ?, ?)',
            (user_id, 'WITHDRAW', amount)
        )

        return jsonify({'success': True, 'message': 'Withdrawal successful', 'new_balance': new_balance}), 200

    except sqlite3.Error as e:
        return jsonify({'success': False, 'error': f'Database error: {str(e)}'}), 500
    except Exception as e:
        return jsonify({'success': False, 'error': f'Unexpected error: {str(e)}'}), 500

@app.route('/member/deposit', methods=['POST'])
def deposit():
    data = request.json
    user_id = data.get('user_id')
    amount = data.get('amount')
    password = data.get('password')

    # 检查请求中的必需字段
    if not user_id or not amount or not password:
        return jsonify({'success': False, 'error': 'Missing required fields'}), 400

    try:
        # 查询用户信息
        user = query_db('SELECT * FROM users WHERE id = ?', (user_id,), one=True)
        if not user:
            return jsonify({'success': False, 'error': 'User not found'}), 404

        # 验证密码
        if not check_password_hash(user['password'], password):
            return jsonify({'success': False, 'error': 'Invalid password'}), 401

        # 更新余额
        new_balance = user['balance'] + amount
        query_db('UPDATE users SET balance = ? WHERE id = ?', (new_balance, user_id))

        # （可选）记录交易历史
        query_db(
            'INSERT INTO transactions (user_id, type, amount) VALUES (?, ?, ?)',
            (user_id, 'DEPOSIT', amount)
        )

        return jsonify({'success': True, 'message': 'Deposit successful', 'new_balance': new_balance}), 200

    except sqlite3.Error as e:
        return jsonify({'success': False, 'error': f'Database error: {str(e)}'}), 500
    except Exception as e:
        return jsonify({'success': False, 'error': f'Unexpected error: {str(e)}'}), 500

@app.route('/admin/modify_user', methods=['POST'])
def modify_user():
    data = request.json
    user_id = data.get('user_id')
    password = data.get('password')
    role = data.get('role')

    if not user_id or not role:
        return jsonify({'success': False, 'error': 'Missing user_id or role'}), 400

    try:
        with sqlite3.connect('bank.db') as conn:
            cursor = conn.cursor()
            if password:
                hashed_password = generate_password_hash(password)
                cursor.execute('UPDATE users SET password = ? WHERE id = ?', (hashed_password, user_id))
            cursor.execute('UPDATE users SET role = ? WHERE id = ?', (role, user_id))
            conn.commit()
            return jsonify({'success': True, 'message': 'User updated successfully!'}), 200
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)}), 500


@app.route('/admin/get_users', methods=['GET'])
def get_users():
    try:
        users = query_db('SELECT id, username, balance, role FROM users')
        users_list = [{'id': user['id'], 'username': user['username'], 
                       'balance': user['balance'], 'role': user['role']} for user in users]
        return jsonify({'success': True, 'data': users_list}), 200
    except Exception as e:
        return jsonify({'success': False, 'error': str(e)}), 500


if __name__ == '__main__':
    app.run(debug=True)
