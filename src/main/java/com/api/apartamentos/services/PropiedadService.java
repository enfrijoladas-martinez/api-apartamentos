package com.api.apartamentos.services;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.api.apartamentos.models.PropiedadModel;
import com.api.apartamentos.repositories.IClienteRepository;
import com.api.apartamentos.repositories.IPropiedadRepository;

@Service
public class PropiedadService extends BaseCrudService<PropiedadModel> {

    private final IPropiedadRepository propiedadRepository;
    private final IClienteRepository clienteRepository;

    public PropiedadService(IPropiedadRepository propiedadRepository, IClienteRepository clienteRepository) {
        super(propiedadRepository);
        this.propiedadRepository = propiedadRepository;
        this.clienteRepository = clienteRepository;
    }

    public List<PropiedadModel> getAllPropiedades() {
        return findAll();
    }

    public Optional<PropiedadModel> getPropiedadById(Long id) {
        return findById(id);
    }

    public PropiedadModel savePropiedad(PropiedadModel propiedad) {
        // Por API solo llega el id del propietario: se carga completo
        if (propiedad.getPropietario() == null || propiedad.getPropietario().getId() == null) {
            throw new IllegalArgumentException("El propietario es obligatorio");
        }
        Long idPropietario = propiedad.getPropietario().getId();
        propiedad.setPropietario(clienteRepository.findById(idPropietario)
                .orElseThrow(() -> new NoSuchElementException("No existe el cliente " + idPropietario)));
        return save(propiedad);
    }

    public void deletePropiedad(Long id) {
        deleteById(id);
    }

    // ACTIVIDAD: Recuperar propiedades por tipo
    public List<PropiedadModel> getPropiedadesPorTipo(String tipo) {
        return propiedadRepository.findByTipoIgnoreCase(tipo);
    }
}
