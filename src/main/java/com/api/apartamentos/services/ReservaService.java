package com.api.apartamentos.services;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.api.apartamentos.models.ReservaModel;
import com.api.apartamentos.models.UnidadRentableModel;
import com.api.apartamentos.repositories.IClienteRepository;
import com.api.apartamentos.repositories.IReservaRepository;
import com.api.apartamentos.repositories.IUnidadRentableRepository;

@Service
public class ReservaService extends BaseCrudService<ReservaModel> {
    private final IReservaRepository reservaRepository;
    private final IUnidadRentableRepository unidadRepository;
    private final IClienteRepository clienteRepository;
    public ReservaService(IReservaRepository repository, IUnidadRentableRepository unidadRepository, IClienteRepository clienteRepository) { super(repository); reservaRepository = repository; this.unidadRepository = unidadRepository; this.clienteRepository = clienteRepository; }

    @Override
    @Transactional
    public ReservaModel save(ReservaModel reserva) {
        if (reserva.getUnidad() == null || reserva.getUnidad().getIdUnidad() == null) throw new IllegalArgumentException("La unidad es obligatoria");
        if (reserva.getCliente() == null || reserva.getCliente().getId() == null) throw new IllegalArgumentException("El cliente es obligatorio");
        if (reserva.getFechaEntrada() == null || reserva.getFechaSalida() == null || !reserva.getFechaSalida().isAfter(reserva.getFechaEntrada())) throw new IllegalArgumentException("El rango de fechas no es válido");
        // Por API solo llega el id de la unidad: se carga completa para conocer su precio
        UnidadRentableModel unidad = unidadRepository.findById(reserva.getUnidad().getIdUnidad())
                .orElseThrow(() -> new NoSuchElementException("No existe la unidad " + reserva.getUnidad().getIdUnidad()));
        reserva.setUnidad(unidad);
        Long idCliente = reserva.getCliente().getId();
        reserva.setCliente(clienteRepository.findById(idCliente)
                .orElseThrow(() -> new NoSuchElementException("No existe el cliente " + idCliente)));
        if (!"CANCELADA".equals(reserva.getEstado()) && reservaRepository.existeSolapamiento(unidad.getIdUnidad(), reserva.getIdReservacion(), reserva.getFechaEntrada(), reserva.getFechaSalida())) throw new IllegalArgumentException("La unidad ya está reservada en ese rango");
        if (reserva.getImporteTotal() == null && unidad.getPrecioNoche() != null) {
            // Noches por fecha de calendario (entrada 15:00, salida 11:00 del tercer día = 3 noches)
            long noches = Math.max(1, ChronoUnit.DAYS.between(reserva.getFechaEntrada().toLocalDate(), reserva.getFechaSalida().toLocalDate()));
            reserva.setImporteTotal(unidad.getPrecioNoche().multiply(BigDecimal.valueOf(noches)));
        }
        return reservaRepository.save(reserva);
    }
}
