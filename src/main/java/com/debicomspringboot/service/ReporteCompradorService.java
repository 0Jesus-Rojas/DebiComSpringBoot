package com.debicomspringboot.service;

import com.debicomspringboot.dto.DeudaPendienteDTO;
import com.debicomspringboot.dto.PagoHistorialDTO;
import com.debicomspringboot.repository.ReporteCompradorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteCompradorService {

    private final ReporteCompradorRepository reporteCompradorRepository;

    public List<PagoHistorialDTO> listarPagosPorUsuario(int idUsuario) {
        List<Object[]> filas = reporteCompradorRepository.listarPagosPorUsuarioCrudo(idUsuario);
        List<PagoHistorialDTO> result = new ArrayList<>();

        for (Object[] row : filas) {
            PagoHistorialDTO dto = new PagoHistorialDTO();
            dto.setIdPago(((Number) row[0]).intValue());
            dto.setIdFactura(((Number) row[1]).intValue());
            dto.setNumeroFactura((String) row[2]);
            dto.setNumeroReferenciaPago((String) row[3]);
            dto.setMontoPagado(row[4] == null ? BigDecimal.ZERO : new BigDecimal(row[4].toString()));
            dto.setFechaPago(toLocalDateTime(row[5]));
            dto.setTipoPago((String) row[6]);
            dto.setTienda((String) row[7]);
            dto.setObservaciones((String) row[8]);
            dto.setOrigen((String) row[9]);
            result.add(dto);
        }
        return result;
    }

    public List<DeudaPendienteDTO> listarDeudasPorUsuario(int idUsuario) {
        List<Object[]> filas = reporteCompradorRepository.listarDeudasPorUsuarioCrudo(idUsuario);
        List<DeudaPendienteDTO> result = new ArrayList<>();

        for (Object[] row : filas) {
            DeudaPendienteDTO dto = new DeudaPendienteDTO();
            dto.setIdSolicitud(((Number) row[0]).intValue());
            dto.setIdFactura(((Number) row[1]).intValue());
            dto.setTienda((String) row[2]);
            dto.setMontoOriginal(row[3] == null ? BigDecimal.ZERO : new BigDecimal(row[3].toString()));
            dto.setSaldoPendiente(row[4] == null ? BigDecimal.ZERO : new BigDecimal(row[4].toString()));
            dto.setFechaVencimiento(toLocalDate(row[5]));
            dto.setEstado((String) row[6]);
            dto.setTipoPrestamo((String) row[7]);
            result.add(dto);
        }
        return result;
    }

    private LocalDateTime toLocalDateTime(Object val) {
        if (val == null) return null;
        if (val instanceof Timestamp ts) return ts.toLocalDateTime();
        if (val instanceof LocalDateTime ldt) return ldt;
        return Timestamp.valueOf(val.toString()).toLocalDateTime();
    }

    private LocalDate toLocalDate(Object val) {
        if (val == null) return null;
        if (val instanceof Date d) return d.toLocalDate();
        if (val instanceof LocalDate ld) return ld;
        return Date.valueOf(val.toString()).toLocalDate();
    }
}