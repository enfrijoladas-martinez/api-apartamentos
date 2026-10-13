package com.api.apartamentos.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.api.apartamentos.models.PropiedadModel;

public interface IPropiedadRepository extends JpaRepository<PropiedadModel, Long> {

    // ACTIVIDAD: propiedades por tipo (Departamento, Casa, ...)
    List<PropiedadModel> findByTipoIgnoreCase(String tipo);
}
