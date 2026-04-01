import React, { useState, useEffect } from 'react';
import './Dashboard.css';

function Dashboard() {

    const [dashboard, setDashboard] = useState(null);

    useEffect(() => {
        fetchDashboard();
    }, []);

    async function fetchDashboard() {
        try {
            const accessToken = localStorage.getItem('accessToken');
            const response = await fetch('http://localhost:8080/dashboard', {
                method: 'GET',
                headers: {
                    Authorization: `Bearer ${accessToken}`
                }
            });
            const data = await response.json();
            setDashboard(data);
        } catch (error) {
            console.error('Error fetching dashboard:', error);
        }
    }

    if (!dashboard) {
        return <p className="dashboard-loading">Loading Dashboard</p>;
    }

    return (
        <div className="dashboard-wrapper">

            {/* Header */}
            <div className="dashboard-header">
                <h2 className="dashboard-title">
                    Sub<span>Track</span>
                </h2>
                <span className="dashboard-badge">Overview</span>
            </div>

            {/* Summary Cards */}
            <div className="summary-grid">
                <div className="summary-card">
                    <div className="card-icon">📦</div>
                    <div className="card-label">Total Subscriptions</div>
                    <div className="card-value">{dashboard.subscriptions}</div>
                </div>

                <div className="summary-card">
                    <div className="card-icon">✅</div>
                    <div className="card-label">Active</div>
                    <div className="card-value">{dashboard.activeSubscriptions}</div>
                </div>

                <div className="summary-card highlight">
                    <div className="card-icon">💰</div>
                    <div className="card-label">Monthly Savings</div>
                    <div className="card-value green">₹{dashboard.monthlySavings}</div>
                </div>
            </div>

            {/* Bottom two-column grid */}
            <div className="bottom-grid">

                {/* Expiring Soon */}
                <div className="section">
                    <div className="section-header">
                        <div className="section-dot red"></div>
                        <h3 className="section-title">Expiring Soon</h3>
                    </div>
                    {dashboard.expiringSubscriptions.length === 0 ? (
                        <div className="empty-state">
                            <span>🎉</span>
                            Nothing expiring soon
                        </div>
                    ) : (
                        <ul className="dash-list">
                            {dashboard.expiringSubscriptions.map((sub) => (
                                <li key={sub.id} className="dash-list-item">
                                    <span className="item-name">{sub.name}</span>
                                    <span className="item-tag">Expiring</span>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>

                {/* Category Expenses */}
                <div className="section">
                    <div className="section-header">
                        <div className="section-dot"></div>
                        <h3 className="section-title">Category Expenses</h3>
                    </div>
                    {Object.keys(dashboard.categoryExpenses).length === 0 ? (
                        <div className="empty-state">
                            <span>📂</span>
                            No categories yet
                        </div>
                    ) : (
                        <ul className="dash-list">
                            {Object.entries(dashboard.categoryExpenses).map(([category, amount]) => (
                                <li key={category} className="dash-list-item">
                                    <span className="item-name">{category}</span>
                                    <span className="item-amount">₹{amount}</span>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>

            </div>

            {/* Top Subscriptions — full width */}
            <div className="section" style={{ marginTop: '16px' }}>
                <div className="section-header">
                    <div className="section-dot green"></div>
                    <h3 className="section-title">Top Subscriptions</h3>
                </div>
                {Object.keys(dashboard.topSubscriptions || {}).length === 0 ? (
                    <div className="empty-state">
                        <span>📋</span>
                        No subscriptions yet
                    </div>
                ) : (
                    <ul className="dash-list">
                        {Object.entries(dashboard.topSubscriptions).map(([name, amount]) => (
                            <li key={name} className="dash-list-item">
                                <span className="item-name">{name}</span>
                                <span className="item-amount">₹{amount}</span>
                            </li>
                        ))}
                    </ul>
                )}
            </div>

        </div>
    );
}

export default Dashboard;