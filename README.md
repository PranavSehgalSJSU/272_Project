# 272_Project
Get alerted

# Signup
POST http://localhost:8080/auth/signup
{
  "username": "",
  "password": "",
  "email": "",
  "phone": "",
  "pushId": "",
  "allowAlerts": true
}

# Login
POST http://localhost:8080/auth/login
{
  "username": "",
  "password": ""
}

# User to User Alert
POST http://localhost:8080/alert/send
{
  "sender": "user1",
  "receiver": "user2",
  "receivers": ["user3", "user4"],
  "token": "auth0 Token",
  "message": "...", 
  "header" : "...",
  "mode": "email/sms/*"
}

# Change allowed alerts
POST http://localhost:8080/auth
{
  "username":"",
  "token":""
}