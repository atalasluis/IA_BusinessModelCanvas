import { useState } from 'react'
import html2pdf from 'html2pdf.js'
import './App.css'

function App() {
  const [contexto, setContexto] = useState('')
  const [parametros, setParametros] = useState('')
  const [jtbd, setJtbd] = useState(null)
  const [canvas, setCanvas] = useState(null)
  const [isLoading, setIsLoading] = useState(false)
  const [errorMsg, setErrorMsg] = useState(null)
  const [isExporting, setIsExporting] = useState(false)

  const handleGenerate = async () => {
    setIsLoading(true)
    setErrorMsg(null)
    setJtbd(null)
    setCanvas(null)
    
    try {
      const response = await fetch('http://localhost:8080/api/generate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ context: contexto, parameters: parametros }),
      })
      
      if (!response.ok) {
        throw new Error('Servidor ocupado o caído.')
      }
      
      const data = await response.json()
      
      if (!data.jtbd_analysis || !data.business_model_canvas) {
          throw new Error('Formato inválido.')
      }

      setJtbd(data.jtbd_analysis)
      setCanvas(data.business_model_canvas)
    } catch (error) {
      console.error(error)
      setErrorMsg("Error al generar. Los servidores podrían estar saturados. Intenta de nuevo.")
    } finally {
      setIsLoading(false)
    }
  }

  const handleDownloadPDF = () => {
    setIsExporting(true)
    const element = document.getElementById('reporte-bmc')
    
    const opt = {
      margin:       0.5,
      filename:     'Business_Model_Canvas.pdf',
      image:        { type: 'jpeg', quality: 0.98 },
      html2canvas:  { scale: 2, backgroundColor: '#0f172a' },
      jsPDF:        { unit: 'in', format: 'a3', orientation: 'landscape' }
    }
    
    html2pdf().set(opt).from(element).save().then(() => {
      setIsExporting(false)
    })
  }

  const renderList = (items) => {
    if (!items || items.length === 0) return <li>No hay datos</li>
    return items.map((item, i) => <li key={i}>{item}</li>)
  }

  return (
    <div className="app-layout">
      {/* PANEL IZQUIERDO */}
      <div className="panel-formulario">
        <h2>Generador BMC AI</h2>
        
        <label>Contexto (Idea o Problema):</label>
        <textarea 
          rows="5" 
          value={contexto} 
          onChange={(e) => setContexto(e.target.value)} 
          placeholder="Ej: App para conectar estudiantes que necesitan tutorías para parciales de la UCB..."
        />

        <label>Parámetros Adicionales:</label>
        <textarea 
          rows="3" 
          value={parametros} 
          onChange={(e) => setParametros(e.target.value)} 
          placeholder="Ej: Presupuesto limitado, uso de códigos QR..."
        />

        <button onClick={handleGenerate} disabled={isLoading || !contexto}>
          {isLoading ? 'Generando...' : 'Generar Canvas'}
        </button>

        {/* Botón de Exportación */}
        {canvas && (
          <button 
            onClick={handleDownloadPDF} 
            disabled={isExporting}
            style={{
              background: isExporting ? '#475569' : '#10b981', 
              marginTop: '15px'
            }}
          >
            {isExporting ? '⏳ Procesando PDF...' : '⬇️ Descargar PDF'}
          </button>
        )}
      </div>

      {/* PANEL DERECHO */}
      <div className="panel-resultados">
        
        {errorMsg && (
          <div className="error-banner">
            <strong>⚠️ Atención:</strong> {errorMsg}
          </div>
        )}

        {!jtbd && !isLoading && !errorMsg && (
          <div className="estado-vacio">
            <p>Ingresa un contexto y presiona "Generar Canvas" para comenzar.</p>
          </div>
        )}
        
        {/* SKELETON LOADING */}
        {isLoading && (
          <div className="skeleton-container">
            <h3 className="skeleton-title skeleton-anim"></h3>
            <div className="skeleton-jtbd skeleton-anim"></div>
            <h3 className="skeleton-title skeleton-anim" style={{marginTop: '30px'}}></h3>
            <div className="canvas-grid">
              <div className="bloque alianzas skeleton-anim"></div>
              <div className="bloque actividades skeleton-anim"></div>
              <div className="bloque recursos skeleton-anim"></div>
              <div className="bloque propuesta skeleton-anim"></div>
              <div className="bloque relaciones skeleton-anim"></div>
              <div className="bloque canales skeleton-anim"></div>
              <div className="bloque segmentos skeleton-anim"></div>
              <div className="bloque costos skeleton-anim"></div>
              <div className="bloque ingresos skeleton-anim"></div>
            </div>
          </div>
        )}
        
        {/* RESULTADOS REALES */}
        {jtbd && canvas && !isLoading && (
          <div id="reporte-bmc" className="resultados-contenedor">
            <div className="jtbd-section">
              <h3>Análisis Jobs To Be Done</h3>
              <ul>
                <li><strong>Job Principal:</strong> {jtbd.job_principal}</li>
                <li><strong>Job Funcional:</strong> {jtbd.job_funcional}</li>
                <li><strong>Job Social:</strong> {jtbd.job_social}</li>
                <li><strong>Job Emocional:</strong> {jtbd.job_emocional}</li>
              </ul>
            </div>

            <h3>Business Model Canvas</h3>
            <div className="canvas-grid-scroll">
              <div className="canvas-grid">
                <div className="bloque alianzas">
                  <h4>Alianzas Clave</h4>
                  <ul>{renderList(canvas.alianzas_clave)}</ul>
                </div>
                <div className="bloque actividades">
                  <h4>Actividades Clave</h4>
                  <ul>{renderList(canvas.actividades_clave)}</ul>
                </div>
                <div className="bloque recursos">
                  <h4>Recursos Clave</h4>
                  <ul>{renderList(canvas.recursos_clave)}</ul>
                </div>
                <div className="bloque propuesta">
                  <h4>Propuesta de Valor</h4>
                  <ul>{renderList(canvas.propuesta_valor)}</ul>
                </div>
                <div className="bloque relaciones">
                  <h4>Relación con Clientes</h4>
                  <ul>{renderList(canvas.relacion_clientes)}</ul>
                </div>
                <div className="bloque canales">
                  <h4>Canales</h4>
                  <ul>{renderList(canvas.canales)}</ul>
                </div>
                <div className="bloque segmentos">
                  <h4>Segmentos de Clientes</h4>
                  <ul>{renderList(canvas.segmentos_clientes)}</ul>
                </div>
                <div className="bloque costos">
                  <h4>Estructura de Costos</h4>
                  <ul>{renderList(canvas.estructura_costos)}</ul>
                </div>
                <div className="bloque ingresos">
                  <h4>Fuentes de Ingresos</h4>
                  <ul>{renderList(canvas.fuentes_ingresos)}</ul>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

export default App
