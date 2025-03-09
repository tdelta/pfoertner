var db = require('../database.js');
var Sequelize = require('sequelize');
var crypto = require('crypto');

var ENCRYPTION_KEY = process.env.TOKEN_ENCRYPTION_KEY
var IV_LENGTH = 16; // AES block size

// Encryption function
function encrypt(text) {
  const iv = crypto.randomBytes(IV_LENGTH);
  const cipher = crypto.createCipheriv('aes-256-cbc', Buffer.from(ENCRYPTION_KEY, 'hex'), iv);
  let encrypted = cipher.update(text, 'utf8', 'hex');
  encrypted += cipher.final('hex');
  return iv.toString('hex') + ':' + encrypted; // Store IV with the ciphertext
}

// Decryption function
function decrypt(text) {
  const textParts = text.split(':');
  const iv = Buffer.from(textParts.shift(), 'hex');
  const encryptedText = Buffer.from(textParts.join(':'), 'hex');
  const decipher = crypto.createDecipheriv('aes-256-cbc', Buffer.from(ENCRYPTION_KEY, 'hex'), iv);
  let decrypted = decipher.update(encryptedText, 'hex', 'utf8');
  decrypted += decipher.final('utf8');
  return decrypted;
}

const GoogleOAuthToken = db.sequelize.define('GoogleOAuthToken', {
  id: {
    type: Sequelize.INTEGER,
    primaryKey: true,
    autoIncrement: true,
  },
  refreshToken: {
    type: Sequelize.TEXT,
    allowNull: false,
    set(value) {
      this.setDataValue('refreshToken', encrypt(value));
    },
    get() {
      const storedValue = this.getDataValue('refreshToken');
      return storedValue ? decrypt(storedValue) : null;
    },
  },
  accessToken: Sequelize.TEXT,
  expiryDate: Sequelize.DATE,
});

module.exports = GoogleOAuthToken;
