import React, { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useOutletContext } from 'react-router-dom';
import html2pdf from 'html2pdf.js';

export default function HistoryPage() {
    const { user } = useAuth();
    const { sessoes } = useOutletContext();
    const [expandedSessao, setExpandedSessao] = useState(null);

    // Processar dados para o gráfico de evolução (apenas sessões finalizadas)
    const evolucao = sessoes
        .filter(s => s.status === 'FINALIZADA' && s.pontuacaoGeral !== null)
        .sort((a, b) => new Date(a.dataInicio) - new Date(b.dataInicio))
        .slice(-5) // Pegar as últimas 5
        .map(s => ({
            data: new Date(s.dataInicio).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' }),
            pontuacao: Math.round(s.pontuacaoGeral)
        }));
    
    // Ordenar sessões para a tabela (mais recente primeiro)
    const sessoesOrdenadas = [...sessoes].sort((a, b) => new Date(b.dataInicio) - new Date(a.dataInicio));

    const toggleDetails = (sessaoId) => {
        if (expandedSessao === sessaoId) {
            setExpandedSessao(null);
        } else {
            setExpandedSessao(sessaoId);
        }
    };

    return (
        <div>
            <h1 style={{color:'#f8fafc',fontSize:28,marginBottom:24}}>📊 Histórico de {user.nome}</h1>

            {/* Gráfico de Evolução */}
            <div className="card" style={{marginBottom:24}}>
                <h2 style={{color:'#f8fafc',fontSize:20,marginBottom:20}}>Evolução da Pontuação</h2>
                {evolucao.length === 0 ? (
                    <p style={{color:'#64748b',textAlign:'center'}}>Nenhum dado de evolução disponível ainda.</p>
                ) : (
                    <div style={{display:'flex',alignItems:'flex-end',gap:16,height:200,padding:'0 20px'}}>
                        {evolucao.map((e, i) => (
                            <div key={i} style={{flex:1,display:'flex',flexDirection:'column',alignItems:'center',gap:8}}>
                                <span style={{color:'#f8fafc',fontSize:14,fontWeight:600}}>{e.pontuacao}%</span>
                                <div style={{width:'100%',background:'linear-gradient(180deg,#6366f1,#8b5cf6)',borderRadius:8,height:`${(e.pontuacao/100)*150}px`,transition:'height 0.5s'}} />
                                <span style={{color:'#64748b',fontSize:12}}>{e.data}</span>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Lista de Sessões */}
            <div className="card">
                <div style={{display:'flex',justifyContent:'space-between',alignItems:'center',marginBottom:20}}>
                    <h2 style={{color:'#f8fafc',fontSize:20,margin:0}}>Sessões Realizadas</h2>
                    <button onClick={() => {
                        const element = document.getElementById('historico-completo');
                        html2pdf().set({ 
                            margin: 10, 
                            filename: `historico-${user.nome}-${Date.now()}.pdf`,
                            image: { type: 'jpeg', quality: 0.98 },
                            html2canvas: { scale: 2, useCORS: true },
                            jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
                        }).from(element).save();
                    }} className="btn btn-secondary">
                        📄 Exportar PDF
                    </button>
                </div>
                <div id="historico-completo" style={{padding: '10px', backgroundColor: '#1e293b'}}>
                    <h3 style={{color:'#94a3b8',marginBottom:16,display:'none'}}>Histórico de {user.nome}</h3>
                    {sessoesOrdenadas.length === 0 ? (
                        <p style={{color:'#64748b',textAlign:'center',padding:32}}>Nenhuma sessão encontrada.</p>
                    ) : (
                        <div style={{display: 'flex', flexDirection: 'column', gap: '12px'}}>
                            {sessoesOrdenadas.map((s) => (
                                <div key={s.id} style={{background: 'rgba(15,23,42,0.6)', borderRadius: '12px', overflow: 'hidden', border: '1px solid rgba(255,255,255,0.05)'}}>
                                    <div 
                                        onClick={() => toggleDetails(s.id)}
                                        style={{display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '16px', cursor: 'pointer', transition: 'background 0.2s'}}
                                        onMouseOver={(e) => e.currentTarget.style.background = 'rgba(255,255,255,0.05)'}
                                        onMouseOut={(e) => e.currentTarget.style.background = 'transparent'}
                                    >
                                        <div style={{display:'flex',alignItems:'center',gap:16, flex: 1}}>
                                            <div style={{fontSize:24, width: 40, textAlign: 'center'}}>
                                                {s.status === 'FINALIZADA' ? (s.pontuacaoGeral >= 80 ? '🏆' : s.pontuacaoGeral >= 60 ? '✅' : '⚠️') : '⏳'}
                                            </div>
                                            <div>
                                                <div style={{color:'#f8fafc',fontWeight:600, fontSize: 16}}>
                                                    {new Date(s.dataInicio).toLocaleDateString('pt-BR')} <span style={{color: '#94a3b8', fontWeight: 400, fontSize: 14}}>às {new Date(s.dataInicio).toLocaleTimeString('pt-BR', {hour: '2-digit', minute:'2-digit'})}</span>
                                                </div>
                                                <div style={{color:'#a5b4fc',fontSize:13, marginTop: 4}}>
                                                    Dificuldade: <span style={{fontWeight: 600}}>{s.dificuldade || 'Geral'}</span>
                                                </div>
                                            </div>
                                        </div>
                                        
                                        <div style={{display: 'flex', alignItems: 'center', gap: 24}}>
                                            <div style={{textAlign: 'right'}}>
                                                <div style={{color: '#94a3b8', fontSize: 12}}>Pontuação</div>
                                                <div style={{color: s.pontuacaoGeral >= 80 ? '#4ade80' : s.pontuacaoGeral >= 60 ? '#fbbf24' : '#f87171', fontWeight: 700, fontSize: 18}}>
                                                    {s.pontuacaoGeral ? Math.round(s.pontuacaoGeral) + '%' : '-'}
                                                </div>
                                            </div>
                                            <div style={{textAlign: 'right', minWidth: 80}}>
                                                <div style={{color: '#94a3b8', fontSize: 12}}>Acertos</div>
                                                <div style={{color: '#f8fafc', fontWeight: 600, fontSize: 16}}>
                                                    {s.totalAcertos}/{s.totalPalavras}
                                                </div>
                                            </div>
                                            <div style={{color: '#64748b', transform: expandedSessao === s.id ? 'rotate(180deg)' : 'rotate(0deg)', transition: 'transform 0.3s'}}>
                                                ▼
                                            </div>
                                        </div>
                                    </div>

                                    {expandedSessao === s.id && (
                                        <div style={{borderTop: '1px solid rgba(255,255,255,0.1)', background: 'rgba(0,0,0,0.2)', padding: '20px', animation: 'fadeIn 0.3s'}}>
                                            {s.feedbackGeral && (
                                                <div style={{marginBottom: 20, background: 'rgba(99,102,241,0.1)', padding: '12px', borderRadius: '8px', borderLeft: '4px solid #6366f1'}}>
                                                    <div style={{color: '#a5b4fc', fontSize: 12, fontWeight: 700, marginBottom: 4, textTransform: 'uppercase'}}>Feedback da IA</div>
                                                    <div style={{color: '#e2e8f0', fontSize: 14, lineHeight: 1.5}}>{s.feedbackGeral}</div>
                                                </div>
                                            )}

                                            {s.detalhes && s.detalhes.length > 0 ? (
                                                <div>
                                                    <h4 style={{color: '#f8fafc', fontSize: 14, marginBottom: 12, borderBottom: '1px solid rgba(255,255,255,0.1)', paddingBottom: 8}}>Detalhes da Pronúncia</h4>
                                                    <div style={{display: 'grid', gap: '8px'}}>
                                                        {s.detalhes.map((d, idx) => (
                                                            <div key={idx} style={{display: 'flex', alignItems: 'center', justifyContent: 'space-between', background: 'rgba(255,255,255,0.03)', padding: '10px 14px', borderRadius: '8px'}}>
                                                                <div style={{display: 'flex', alignItems: 'center', gap: 12}}>
                                                                    <span style={{fontSize: 18}}>{d.acertou ? '✅' : '❌'}</span>
                                                                    <div>
                                                                        <div style={{color: '#f8fafc', fontWeight: 600}}>{d.palavraEsperada}</div>
                                                                        {!d.acertou && d.palavraTranscrita && (
                                                                            <div style={{color: '#f87171', fontSize: 12}}>Ouvi: "{d.palavraTranscrita}"</div>
                                                                        )}
                                                                    </div>
                                                                </div>
                                                                {d.feedback && (
                                                                    <div style={{color: '#94a3b8', fontSize: 13, fontStyle: 'italic', maxWidth: '50%', textAlign: 'right'}}>
                                                                        "{d.feedback}"
                                                                    </div>
                                                                )}
                                                            </div>
                                                        ))}
                                                    </div>
                                                </div>
                                            ) : (
                                                <p style={{color: '#64748b', fontSize: 13, textAlign: 'center', fontStyle: 'italic'}}>Detalhes detalhados não disponíveis para esta sessão.</p>
                                            )}
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
            <style>{`
                @keyframes fadeIn { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
            `}</style>
        </div>
    );
}
