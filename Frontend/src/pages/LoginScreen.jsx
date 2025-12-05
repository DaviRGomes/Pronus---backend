import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth, API } from '../contexts/AuthContext';

export default function LoginScreen() {
    const [loginInput, setLoginInput] = useState('');
    const [senha, setSenha] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const { login } = useAuth();
    const navigate = useNavigate();

    const submit = async (e) => {
        e.preventDefault();
        setLoading(true); setError('');
        try {
            const res = await fetch(`${API}/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ login: loginInput, senha })
            });
            if (!res.ok) throw new Error('Credenciais inválidas');
            const data = await res.json();
            login({ id: data.id, nome: data.nome, tipo: data.tipoUsuario }, data.token);
            navigate('/');
        } catch (err) {
            setError(err.message);
        }
        setLoading(false);
    };

    return (
        <div style={{display:'flex',alignItems:'center',justifyContent:'center',minHeight:'100vh',padding:20}}>
            <div className="card" style={{maxWidth:420,width:'100%',padding:48}}>
                <div style={{textAlign:'center',marginBottom:32}}>
                    <div style={{fontSize:64,marginBottom:16}}>🗣️</div>
                    <h1 style={{color:'#f8fafc',fontSize:32,margin:0}}>FonoFlow</h1>
                    <p style={{color:'#94a3b8',marginTop:8}}>Treino de pronúncia com IA</p>
                </div>
                <form onSubmit={submit}>
                    <div style={{marginBottom:20}}>
                        <label style={{color:'#e2e8f0',display:'block',marginBottom:8,fontSize:14}}>Login</label>
                        <input className="input" value={loginInput} onChange={e => setLoginInput(e.target.value)} placeholder="Digite seu login" required />
                    </div>
                    <div style={{marginBottom:20}}>
                        <label style={{color:'#e2e8f0',display:'block',marginBottom:8,fontSize:14}}>Senha</label>
                        <input className="input" type="password" value={senha} onChange={e => setSenha(e.target.value)} placeholder="Digite sua senha" required />
                    </div>
                    {error && <div style={{background:'rgba(239,68,68,0.2)',borderRadius:12,padding:12,color:'#fca5a5',marginBottom:20}}>⚠️ {error}</div>}
                    <button type="submit" className="btn btn-primary" disabled={loading} style={{width:'100%',padding:16,fontSize:16}}>
                        {loading ? '⏳ Entrando...' : 'Entrar'}
                    </button>
                </form>
                <div style={{marginTop:24,textAlign:'center'}}>
                    <p style={{color:'#64748b',fontSize:13}}>Ainda não tem conta?</p>
                    <button onClick={() => navigate('/register')} className="btn btn-secondary" style={{marginTop:8}}>
                        Criar Conta
                    </button>
                </div>
            </div>
        </div>
    );
}
