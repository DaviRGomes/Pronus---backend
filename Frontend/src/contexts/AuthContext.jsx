import React, { createContext, useState, useEffect, useContext } from 'react';

const AuthContext = createContext();

export const API = 'http://localhost:8080';

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [token, setToken] = useState(localStorage.getItem('fono_token'));
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const u = localStorage.getItem('fono_user');
        const t = localStorage.getItem('fono_token');
        if (u && t) {
            setUser(JSON.parse(u));
            setToken(t);
        }
        setLoading(false);
    }, []);

    const login = (userData, authToken) => {
        setUser(userData);
        setToken(authToken);
        localStorage.setItem('fono_token', authToken);
        localStorage.setItem('fono_user', JSON.stringify(userData));
    };

    const logout = () => {
        setUser(null);
        setToken(null);
        localStorage.removeItem('fono_token');
        localStorage.removeItem('fono_user');
    };

    return (
        <AuthContext.Provider value={{ user, token, login, logout, loading }}>
            {!loading && children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    return useContext(AuthContext);
}
