import React, { useState, useEffect } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export default function HomePage() {
    const { user } = useAuth();
    const { sessoes } = useOutletContext();
    const navigate = useNavigate();
    const [dashboardData, setDashboardData] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchDashboardData();
    }, [user.id]);

    const fetchDashboardData = async () => {
        try {
            const response = await fetch(`http://localhost:8080/api/sessao-treino/dashboard/${user.id}`);
            if (response.ok) {
                const data = await response.json();
                setDashboardData(data);
            } else {
                console.error('Erro ao buscar dados do dashboard');
            }
        } catch (error) {
            console.error('Erro na requisição do dashboard:', error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return <div style={{color: '#fff'}}>Carregando dados...</div>;
    }

    // Se tiver dados do backend usa eles, senão usa fallback
    const totalSessoes = dashboardData ? dashboardData.sessoesRealizadas : 0;
    const pontuacaoMedia = dashboardData ? dashboardData.pontuacaoMedia : 0;
    const sequencia = dashboardData ? dashboardData.diasSeguidos : 0;

    return (
        <div>
            <div style={{marginBottom:32}}>
                <h1 style={{color:'#f8fafc',fontSize:28,margin:0}}>Olá, {user.nome.split(' ')[0]}! 👋</h1>
                <p style={{color:'#94a3b8',marginTop:8}}>Pronto para melhorar sua pronúncia hoje?</p>
            </div>

            {/* Stats */}
            <div style={{display:'grid',gridTemplateColumns:'repeat(auto-fit, minmax(200px, 1fr))',gap:20,marginBottom:32}}>
                <div className="stat-card">
                    <div className="stat-value">{totalSessoes}</div>
                    <div className="stat-label">Sessões Realizadas</div>
                </div>
                <div className="stat-card">
                    <div className="stat-value">{pontuacaoMedia}%</div>
                    <div className="stat-label">Pontuação Média</div>
                </div>
                <div className="stat-card">
                    <div className="stat-value">{sequencia} 🔥</div>
                    <div className="stat-label">Dias Seguidos</div>
                </div>
            </div>

            {/* Quick Actions */}
            <div className="card" style={{marginBottom:32}}>
                <h2 style={{color:'#f8fafc',fontSize:20,marginBottom:20}}>Iniciar Treino Rápido</h2>
                <p style={{color:'#94a3b8',marginBottom:24}}>Escolha uma dificuldade e comece a treinar agora mesmo!</p>
                <button onClick={() => navigate('/session')} className="btn btn-primary" style={{display:'flex',alignItems:'center',gap:12,padding:'16px 32px',fontSize:16}}>
                    <span style={{fontSize:24}}>🎤</span>
                    Iniciar Nova Sessão
                </button>
            </div>

            {/* Recent Activity */}
            <div className="card">
                <h2 style={{color:'#f8fafc',fontSize:20,marginBottom:20}}>Atividade Recente</h2>
                {sessoes.length === 0 ? (
                    <p style={{color:'#64748b',textAlign:'center',padding:32}}>Nenhuma sessão realizada ainda. Comece agora! 🚀</p>
                ) : (
                    <div style={{display:'flex',flexDirection:'column',gap:12}}>
                        {sessoes.slice(0, 5).map((s, i) => (
                            <div key={i} style={{display:'flex',alignItems:'center',justifyContent:'space-between',padding:16,background:'rgba(15,23,42,0.6)',borderRadius:12}}>
                                <div style={{display:'flex',alignItems:'center',gap:12}}>
                                    <span style={{fontSize:24}}>📅</span>
                                    <div>
                                        <div style={{color:'#f8fafc',fontWeight:500}}>{s.dificuldade ? `Treino: ${s.dificuldade}` : 'Sessão de Treino'}</div>
                                        <div style={{color:'#64748b',fontSize:13}}>{new Date(s.dataInicio).toLocaleString('pt-BR')}</div>
                                    </div>
                                </div>
                                <span className={`badge badge-${s.status === 'FINALIZADA' ? 'success' : 'warning'}`}>{s.status}</span>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}

