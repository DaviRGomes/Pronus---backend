import React from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useOutletContext } from 'react-router-dom';

export default function ProfilePage() {
    const { user } = useAuth();
    const { especialistas, selectedEsp, setSelectedEsp } = useOutletContext();

    return (
        <div>
            <h1 style={{color:'#f8fafc',fontSize:28,marginBottom:24}}>👤 Meu Perfil</h1>

            <div className="card" style={{marginBottom:24}}>
                <h2 style={{color:'#f8fafc',fontSize:20,marginBottom:20}}>Informações Pessoais</h2>
                <div style={{display:'grid',gap:16}}>
                    <div>
                        <label style={{color:'#94a3b8',fontSize:13,display:'block',marginBottom:8}}>Nome</label>
                        <div style={{color:'#f8fafc',fontSize:16,fontWeight:500}}>{user.nome}</div>
                    </div>
                    <div>
                        <label style={{color:'#94a3b8',fontSize:13,display:'block',marginBottom:8}}>Tipo de Usuário</label>
                        <div style={{color:'#f8fafc',fontSize:16,fontWeight:500}}>{user.tipo}</div>
                    </div>
                    <div>
                        <label style={{color:'#94a3b8',fontSize:13,display:'block',marginBottom:8}}>ID</label>
                        <div style={{color:'#f8fafc',fontSize:16,fontWeight:500}}>{user.id}</div>
                    </div>
                </div>
            </div>

            <div className="card">
                <h2 style={{color:'#f8fafc',fontSize:20,marginBottom:20}}>Fonoaudiólogo Selecionado</h2>
                <div style={{display:'grid',gridTemplateColumns:'repeat(auto-fill,minmax(280px,1fr))',gap:16}}>
                    {especialistas.map(esp => (
                        <div key={esp.id} onClick={() => setSelectedEsp(esp)}
                            style={{display:'flex',alignItems:'center',gap:16,padding:16,
                            background: selectedEsp?.id === esp.id ? 'rgba(99,102,241,0.2)' : 'rgba(15,23,42,0.6)',
                            borderRadius:12,border: selectedEsp?.id === esp.id ? '2px solid #6366f1' : '2px solid transparent',
                            cursor:'pointer',transition:'all 0.2s'}}>
                            <span style={{fontSize:40}}>👩‍⚕️</span>
                            <div>
                                <div style={{color:'#f8fafc',fontWeight:600}}>{esp.nome}</div>
                                <div style={{color:'#a5b4fc',fontSize:13}}>{esp.especialidade || 'Fonoaudiologia'}</div>
                                <div style={{color:'#64748b',fontSize:12}}>CRM: {esp.crmFono || 'N/A'}</div>
                            </div>
                            {selectedEsp?.id === esp.id && <span style={{marginLeft:'auto',color:'#6366f1',fontSize:24}}>✓</span>}
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}
