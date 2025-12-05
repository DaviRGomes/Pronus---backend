import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { API } from '../contexts/AuthContext';

export default function RegisterScreen() {
    const [form, setForm] = useState({ nome: '', idade: '', login: '', senha: '', nivel: 'INICIANTE' });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState(false);
    const navigate = useNavigate();

    const submit = async (e) => {
        e.preventDefault();
        setLoading(true); setError('');
        try {
            const res = await fetch(`${API}/clientes`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ ...form, idade: parseInt(form.idade) })
            });
            if (!res.ok) throw new Error('Erro ao criar conta');
            setSuccess(true);
            setTimeout(() => navigate('/login'), 2000);
        } catch (err) {
            setError(err.message);
        }
        setLoading(false);
    };

    if (success) {
        return (
            <div style={{display:'flex',alignItems:'center',justifyContent:'center',minHeight:'100vh'}}>
                <div className="card" style={{textAlign:'center',padding:48}}>
                    <div style={{fontSize:64,marginBottom:16}}>✅</div>
                    <h2 style={{color:'#22c55e',margin:0}}>Conta criada com sucesso!</h2>
                    <p style={{color:'#94a3b8',marginTop:12}}>Redirecionando para o login...</p>
                </div>
            </div>
        );
    }

    return (
        <div style={{display:'flex',alignItems:'center',justifyContent:'center',minHeight:'100vh',padding:20}}>
            <div className="card" style={{maxWidth:420,width:'100%',padding:48}}>
                <button onClick={() => navigate('/login')} style={{background:'none',border:'none',color:'#94a3b8',cursor:'pointer',marginBottom:16}}>← Voltar</button>
                <h2 style={{color:'#f8fafc',marginBottom:24}}>Criar Conta</h2>
                <form onSubmit={submit}>
                    {[
                        { key: 'nome', label: 'Nome Completo', type: 'text', placeholder: 'Seu nome' },
                        { key: 'idade', label: 'Idade', type: 'number', placeholder: 'Sua idade' },
                        { key: 'login', label: 'Login', type: 'text', placeholder: 'Escolha um login' },
                        { key: 'senha', label: 'Senha', type: 'password', placeholder: 'Escolha uma senha' },
                    ].map(f => (
                        <div key={f.key} style={{marginBottom:16}}>
                            <label style={{color:'#e2e8f0',display:'block',marginBottom:8,fontSize:14}}>{f.label}</label>
                            <input className="input" type={f.type} value={form[f.key]} onChange={e => setForm({...form, [f.key]: e.target.value})} placeholder={f.placeholder} required />
                        </div>
                    ))}
                    {error && <div style={{background:'rgba(239,68,68,0.2)',borderRadius:12,padding:12,color:'#fca5a5',marginBottom:20}}>⚠️ {error}</div>}
                    <button type="submit" className="btn btn-primary" disabled={loading} style={{width:'100%',padding:16}}>
                        {loading ? '⏳ Criando...' : 'Criar Conta'}
                    </button>
                </form>
            </div>
        </div>
    );
}
