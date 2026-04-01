import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Login.css';

function Register() {
  const navigate = useNavigate();

  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [planType, setPlanType] = useState('');
  const [password, setPassword] = useState('');

  async function handleRegister(e) {
    e.preventDefault();

    try {
      const res = await fetch('http://localhost:8080/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name,
          email,
          planType,
          password
        }),
      });

      const data = await res.json();

      if (!res.ok) {
        throw new Error(data.message || 'Registration failed');
      }

      alert('Registration successful!');
      navigate('/');
    } catch (err) {
      alert('Registration failed: ' + err.message);
    }
  }

  return (
    <div className="login-container">
      <div className="login-card">
        <h2 className="login-title">Register</h2>

        <form onSubmit={handleRegister}>
          
          <div className="form-group">
            <label>Name:</label>
            <input
              className="input-field"
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              placeholder="Enter your name"
            />
          </div>

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
            <label>Plan Type:</label>
            <select
              className="input-field"
              value={planType}
              onChange={(e) => setPlanType(e.target.value)}
              required
            >
              <option value="">Select Plan</option>
              <option value="FREE">FREE</option>
              <option value="PRO">PRO</option>
            </select>
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
            Register
          </button>
        </form>
      </div>
    </div>
  );
}

export default Register;