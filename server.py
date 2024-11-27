from flask import Flask, request
from Crypto.Cipher import AES
import base64

app = Flask(__name__)
key = b'your-16-byte-key'

def encrypt_message(message):
    cipher = AES.new(key, AES.MODE_ECB)
    return base64.b64encode(cipher.encrypt(message.ljust(16).encode()))

def decrypt_message(encrypted_message):
    cipher = AES.new(key, AES.MODE_ECB)
    return cipher.decrypt(base64.b64decode(encrypted_message)).strip().decode()

@app.route('/send_to_client', methods=['GET'])
def send_to_client():
    message = "Hello Client!"
    encrypted_message = encrypt_message(message)
    return encrypted_message

@app.route('/receive_from_client', methods=['POST'])
def receive_from_client():
    encrypted_message = request.data
    decrypted_message = decrypt_message(encrypted_message)
    print(f"Decrypted message from client: {decrypted_message}")
    return "Message received"

if __name__ == '__main__':
    app.run(port=5000)
