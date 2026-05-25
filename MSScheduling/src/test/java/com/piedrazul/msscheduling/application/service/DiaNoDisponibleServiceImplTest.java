package com.piedrazul.msscheduling.application.service;

import com.piedrazul.msscheduling.application.service.impl.DiaNoDisponibleServiceImpl;
import com.piedrazul.msscheduling.domain.model.dto.DiaNoDisponibleDTO;
import com.piedrazul.msscheduling.domain.model.entity.DiaNoDisponible;
import com.piedrazul.msscheduling.domain.model.entity.enums.TipoDiaNoDisponible;
import com.piedrazul.msscheduling.domain.model.repository.DiaNoDisponibleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DiaNoDisponibleServiceImpl")
class DiaNoDisponibleServiceImplTest {

    @Mock  private DiaNoDisponibleRepository repository;
    @InjectMocks private DiaNoDisponibleServiceImpl service;

    // -----------------------------------------------------------------------
    // registrar()
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("registrar()")
    class RegistrarTest {

        @Test
        @DisplayName("Persiste la entidad y retorna el DTO con los mismos datos")
        void registrar_persisteYRetornaDTO() {
            LocalDate fecha = LocalDate.of(2025, 12, 25);
            DiaNoDisponibleDTO dto = dto(fecha, "Navidad", TipoDiaNoDisponible.FESTIVO);

            when(repository.existsByFecha(fecha)).thenReturn(false);
            when(repository.save(any())).thenAnswer(inv -> {
                DiaNoDisponible e = inv.getArgument(0);
                e.setId(1L);
                return e;
            });

            DiaNoDisponibleDTO resultado = service.registrar(dto);

            assertThat(resultado.getFecha()).isEqualTo(fecha);
            assertThat(resultado.getMotivo()).isEqualTo("Navidad");
            assertThat(resultado.getTipo()).isEqualTo(TipoDiaNoDisponible.FESTIVO);
            verify(repository).save(any());
        }

        @Test
        @DisplayName("Lanza IllegalArgumentException cuando la fecha ya está registrada")
        void fechaDuplicada_lanzaExcepcion() {
            LocalDate fecha = LocalDate.of(2025, 1, 1);
            when(repository.existsByFecha(fecha)).thenReturn(true);

            assertThatThrownBy(() -> service.registrar(dto(fecha, "Año nuevo", TipoDiaNoDisponible.FESTIVO)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(fecha.toString());
        }

        @Test
        @DisplayName("Mensaje de excepción menciona la fecha duplicada")
        void mensajeExcepcion_mencionaFecha() {
            LocalDate fecha = LocalDate.of(2025, 7, 20);
            when(repository.existsByFecha(fecha)).thenReturn(true);

            assertThatThrownBy(() -> service.registrar(dto(fecha, "Festivo", TipoDiaNoDisponible.FESTIVO)))
                    .hasMessageContaining("2025-07-20");
        }

        @Test
        @DisplayName("No llama a save() cuando la fecha ya existe")
        void fechaDuplicada_noLlamaASave() {
            LocalDate fecha = LocalDate.now().plusDays(5);
            when(repository.existsByFecha(fecha)).thenReturn(true);

            try {
                service.registrar(dto(fecha, "bloqueo", TipoDiaNoDisponible.BLOQUEO_MANUAL));
            } catch (IllegalArgumentException ignored) { }

            verify(repository, never()).save(any());
        }
    }

    // -----------------------------------------------------------------------
    // listarTodos()
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("listarTodos()")
    class ListarTodosTest {

        @Test
        @DisplayName("Retorna lista vacía cuando no hay días registrados")
        void sinRegistros_retornaListaVacia() {
            when(repository.findAll()).thenReturn(List.of());
            assertThat(service.listarTodos()).isEmpty();
        }

        @Test
        @DisplayName("Retorna los días ordenados por fecha ascendente")
        void conRegistros_retornaOrdenados() {
            LocalDate hoy = LocalDate.now();
            DiaNoDisponible dia1 = entidad(1L, hoy.plusDays(10), "B", TipoDiaNoDisponible.BLOQUEO_MANUAL);
            DiaNoDisponible dia2 = entidad(2L, hoy.plusDays(1),  "A", TipoDiaNoDisponible.FESTIVO);

            when(repository.findAll()).thenReturn(List.of(dia1, dia2));

            List<DiaNoDisponibleDTO> resultado = service.listarTodos();

            assertThat(resultado).hasSize(2);
            assertThat(resultado.get(0).getFecha()).isEqualTo(hoy.plusDays(1));
            assertThat(resultado.get(1).getFecha()).isEqualTo(hoy.plusDays(10));
        }

        @Test
        @DisplayName("Mapea correctamente tipo y motivo de cada día")
        void mapea_tipoYMotivo() {
            DiaNoDisponible dia = entidad(1L, LocalDate.now().plusDays(3), "Festivo col",
                    TipoDiaNoDisponible.FESTIVO);
            when(repository.findAll()).thenReturn(List.of(dia));

            DiaNoDisponibleDTO resultado = service.listarTodos().get(0);

            assertThat(resultado.getTipo()).isEqualTo(TipoDiaNoDisponible.FESTIVO);
            assertThat(resultado.getMotivo()).isEqualTo("Festivo col");
        }
    }

    // -----------------------------------------------------------------------
    // listarEnRango()
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("listarEnRango()")
    class ListarEnRangoTest {

        @Test
        @DisplayName("Delega en findByFechaBetween con los parámetros correctos")
        void delega_enRepositorio() {
            LocalDate desde = LocalDate.now();
            LocalDate hasta = desde.plusMonths(1);

            when(repository.findByFechaBetween(desde, hasta)).thenReturn(List.of());

            service.listarEnRango(desde, hasta);

            verify(repository).findByFechaBetween(desde, hasta);
        }

        @Test
        @DisplayName("Retorna lista ordenada por fecha cuando hay resultados")
        void conResultados_retornaOrdenados() {
            LocalDate base = LocalDate.now();
            DiaNoDisponible d1 = entidad(1L, base.plusDays(5), "m1", TipoDiaNoDisponible.FESTIVO);
            DiaNoDisponible d2 = entidad(2L, base.plusDays(2), "m2", TipoDiaNoDisponible.BLOQUEO_MANUAL);

            when(repository.findByFechaBetween(any(), any())).thenReturn(List.of(d1, d2));

            List<DiaNoDisponibleDTO> resultado = service.listarEnRango(base, base.plusMonths(1));

            assertThat(resultado.get(0).getFecha()).isEqualTo(base.plusDays(2));
            assertThat(resultado.get(1).getFecha()).isEqualTo(base.plusDays(5));
        }
    }

    // -----------------------------------------------------------------------
    // eliminar()
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("eliminar()")
    class EliminarTest {

        @Test
        @DisplayName("Llama a deleteById cuando el día existe")
        void diaExiste_llamaDeleteById() {
            DiaNoDisponible dia = entidad(10L, LocalDate.now(), "bloqueo", TipoDiaNoDisponible.BLOQUEO_MANUAL);
            when(repository.findById(10L)).thenReturn(Optional.of(dia));

            service.eliminar(10L);

            verify(repository).deleteById(10L);
        }

        @Test
        @DisplayName("Lanza IllegalArgumentException cuando el ID no existe")
        void diaNoExiste_lanzaExcepcion() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.eliminar(99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("No llama a deleteById cuando el ID no existe")
        void diaNoExiste_noLlamaDeleteById() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            try {
                service.eliminar(99L);
            } catch (IllegalArgumentException ignored) { }

            verify(repository, never()).deleteById(any());
        }
    }

    // -----------------------------------------------------------------------
    // esFechaNoDisponible()
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("esFechaNoDisponible()")
    class EsFechaNoDisponibleTest {

        @Test
        @DisplayName("Retorna true cuando la fecha está bloqueada")
        void fechaBloqueada_retornaTrue() {
            LocalDate fecha = LocalDate.of(2025, 12, 25);
            when(repository.existsByFecha(fecha)).thenReturn(true);

            assertThat(service.esFechaNoDisponible(fecha)).isTrue();
        }

        @Test
        @DisplayName("Retorna false cuando la fecha no está bloqueada")
        void fechaLibre_retornaFalse() {
            LocalDate fecha = LocalDate.of(2025, 12, 26);
            when(repository.existsByFecha(fecha)).thenReturn(false);

            assertThat(service.esFechaNoDisponible(fecha)).isFalse();
        }

        @Test
        @DisplayName("Delega directamente en existsByFecha del repositorio")
        void delega_enRepositorio() {
            LocalDate fecha = LocalDate.now().plusDays(7);
            when(repository.existsByFecha(fecha)).thenReturn(false);

            service.esFechaNoDisponible(fecha);

            verify(repository).existsByFecha(fecha);
        }
    }

    // -----------------------------------------------------------------------
    // Fixtures
    // -----------------------------------------------------------------------
    private DiaNoDisponibleDTO dto(LocalDate fecha, String motivo, TipoDiaNoDisponible tipo) {
        return DiaNoDisponibleDTO.builder()
                .fecha(fecha).motivo(motivo).tipo(tipo).build();
    }

    private DiaNoDisponible entidad(Long id, LocalDate fecha, String motivo, TipoDiaNoDisponible tipo) {
        DiaNoDisponible e = new DiaNoDisponible();
        e.setId(id);
        e.setFecha(fecha);
        e.setMotivo(motivo);
        e.setTipo(tipo);
        return e;
    }
}
