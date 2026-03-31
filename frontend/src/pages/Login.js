import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

function Login() {

    const navigate = useNavigate();

    //State for email and password
    cost [email, setEmail] = useState('')
    const [password, setPassword] = useState('');

    //handle form submission
    async function handleSubmit(e) {
        //prevent page refresh
        e.preventDefault();

        try{
            const response = await fetch('http://localhost:8080/api/login', {
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
            localStorage.setItem('accessToken',data.accessToken);
            localStorage.setItem('refreshToken', data.refreshToken);
            localStorage.setItem('email', email);

            //Navigate to dashboard
            navigate('/dashboard');
        }catch (error) {
            console.error('Error during login:', error);
    }
}

    return (
        <div className="login-container">
            <h2>Login</h2>
            <form onSubmit = {handleSubmit}>
                <div>
                    <label>Email:</label>
                    <input
                        type = "email"
                        value = {email}
                        onChange = {(e) => setEmail(e.target.value)}
                        required
                        placeholder = "Enter your email"
                    />
                </div>

                <div>
                    <label>Password:</label>
                    <input
                        type = "password"
                        value ={password}
                        onChange = {(e) => setPassowrd(e.target.value)}
                        required
                        plavceholder = "Enter your password"
                    />
                </div>
                <button type = "submit">Login</button>
            </form>
        </div>
    )

}

export default Login;