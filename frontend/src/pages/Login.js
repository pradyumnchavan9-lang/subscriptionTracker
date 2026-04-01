import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Login.css';

function Login() {

    const navigate = useNavigate();

    //State for email and password
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('');

    //handle form submission
    async function handleSubmit(e) {
        //prevent page refresh
        e.preventDefault();

        try{
            const response = await fetch('http://localhost:8080/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email, password }),
            });

            const data = await response.json();

            if(!response.ok){
                throw new Error('Login failed');
            }
            
            //Save tokens and email in local storage
            localStorage.setItem('accessToken',data.body.accessToken);
            localStorage.setItem('refreshToken', data.body.refreshToken);
            localStorage.setItem('email', data.body.email);

            //Navigate to dashboard
            navigate('/dashboard');
        }catch (error) {
            console.error('Error during login:', error);
    }
}

    return (
  <div className="login-container">
    <div className="login-card">
      <h2 className="login-title">Login</h2>

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Email:</label>
          <input
            className="input-field"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            placeholder="Enter your email"
          />
        </div>

        <div className="form-group">
          <label>Password:</label>
          <input
            className="input-field"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            placeholder="Enter your password"
          />
        </div>

        <button className="login-button" type="submit">
          Login
        </button>
      </form>
    </div>
  </div>
);

}

export default Login;