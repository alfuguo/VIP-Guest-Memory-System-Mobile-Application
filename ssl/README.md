# SSL Certificates Directory

This directory is for SSL certificates used in production deployment.

## For Development
SSL is not required for local development. The nginx service is optional and can be disabled.

## For Production
Place your SSL certificate files here:

- `cert.pem` - Your SSL certificate (full chain)
- `private.key` - Your private key

## Obtaining SSL Certificates

### Using Let's Encrypt (Recommended)
```bash
# Install certbot
sudo apt install certbot

# Obtain certificate
sudo certbot certonly --standalone -d yourdomain.com

# Copy certificates
sudo cp /etc/letsencrypt/live/yourdomain.com/fullchain.pem ./ssl/cert.pem
sudo cp /etc/letsencrypt/live/yourdomain.com/privkey.pem ./ssl/private.key
```

### Using Other Certificate Authorities
Follow your certificate authority's instructions and place the files as:
- Certificate file → `cert.pem`
- Private key file → `private.key`

## Security Notes
- Never commit actual certificate files to version control
- Ensure proper file permissions (600 for private key)
- Regularly renew certificates before expiration