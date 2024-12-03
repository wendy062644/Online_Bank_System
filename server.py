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

if __name__ == '__main__':
    app.run(debug=True)
