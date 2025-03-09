console.log("# public URL must not end with a slash (\"/\")")
console.log("PUBLIC_URL='https://my-pfoertner-server:3000'")
console.log("JWT_SECRET='"+require('crypto').randomBytes(32).toString('hex')+"'")
console.log("TOKEN_ENCRYPTION_KEY='"+require('crypto').randomBytes(32).toString('hex')+"'")
