from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy
from werkzeug.security import generate_password_hash, check_password_hash

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///bank.db'
db = SQLAlchemy(app)

# Database Models
class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(50), unique=True, nullable=False)
    password_hash = db.Column(db.String(255), nullable=False)
    name = db.Column(db.String(100), nullable=False)
    balance = db.Column(db.Float, default=0.0)

class Transaction(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    amount = db.Column(db.Float, nullable=False)
    transaction_type = db.Column(db.String(50), nullable=False)
    created_at = db.Column(db.DateTime, default=db.func.current_timestamp())

# Routes
@app.route('/register', methods=['POST'])
def register():
    data = request.json
    hashed_password = generate_password_hash(data['password'], method='sha256')
    user = User(username=data['username'], password_hash=hashed_password, name=data['name'])
    db.session.add(user)
    db.session.commit()
    return jsonify({"message": "User registered successfully!"})

@app.route('/login', methods=['POST'])
def login():
    data = request.json
    user = User.query.filter_by(username=data['username']).first()
    if user and check_password_hash(user.password_hash, data['password']):
        return jsonify({"message": "Login successful!", "balance": user.balance})
    return jsonify({"message": "Invalid username or password"}), 401

@app.route('/deposit', methods=['POST'])
def deposit():
    data = request.json
    user = User.query.filter_by(username=data['username']).first()
    if user:
        user.balance += data['amount']
        transaction = Transaction(user_id=user.id, amount=data['amount'], transaction_type='deposit')
        db.session.add(transaction)
        db.session.commit()
        return jsonify({"message": "Deposit successful!", "balance": user.balance})
    return jsonify({"message": "User not found"}), 404

@app.route('/withdraw', methods=['POST'])
def withdraw():
    data = request.json
    user = User.query.filter_by(username=data['username']).first()
    if user and user.balance >= data['amount']:
        user.balance -= data['amount']
        transaction = Transaction(user_id=user.id, amount=-data['amount'], transaction_type='withdrawal')
        db.session.add(transaction)
        db.session.commit()
        return jsonify({"message": "Withdrawal successful!", "balance": user.balance})
    return jsonify({"message": "Insufficient balance or user not found"}), 400

if __name__ == '__main__':
    with app.app_context():  # 設置應用程式上下文
        db.create_all()  # 創建資料表
    app.run(port=5000, debug=True)

