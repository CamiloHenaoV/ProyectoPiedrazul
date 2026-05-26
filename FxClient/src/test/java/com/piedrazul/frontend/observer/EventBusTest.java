package com.piedrazul.frontend.observer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EventBus — Patrón Observer")
class EventBusTest {

    private EventBus bus;

    @BeforeEach
    void setUp() {
        bus = new EventBus();
    }

    // -----------------------------------------------------------------------
    // subscribe / publish
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("subscribe() + publish()")
    class SubscribePublishTest {

        @Test
        @DisplayName("Un observer suscrito recibe el evento publicado")
        void observerSuscrito_recibeEvento() {
            List<Object> recibidos = new ArrayList<>();
            bus.subscribe(AppEvent.CITA_AGENDADA, (e, d) -> recibidos.add(d));

            bus.publish(AppEvent.CITA_AGENDADA, "payload-cita");

            assertThat(recibidos).containsExactly("payload-cita");
        }

        @Test
        @DisplayName("Múltiples observers del mismo evento reciben la notificación")
        void multiplesObservers_todosReciben() {
            List<String> recibidos = new ArrayList<>();
            bus.subscribe(AppEvent.USUARIO_CREADO, (e, d) -> recibidos.add("obs1:" + d));
            bus.subscribe(AppEvent.USUARIO_CREADO, (e, d) -> recibidos.add("obs2:" + d));

            bus.publish(AppEvent.USUARIO_CREADO, "usuario-1");

            assertThat(recibidos).containsExactlyInAnyOrder("obs1:usuario-1", "obs2:usuario-1");
        }

        @Test
        @DisplayName("El payload publicado llega íntegro al observer")
        void payload_llegaIntegro() {
            List<Object> captura = new ArrayList<>();
            bus.subscribe(AppEvent.CITA_ACTUALIZADA, (e, d) -> captura.add(d));

            Object payload = new Object();
            bus.publish(AppEvent.CITA_ACTUALIZADA, payload);

            assertThat(captura.get(0)).isSameAs(payload);
        }

        @Test
        @DisplayName("El event recibido coincide con el evento publicado")
        void eventRecibido_coincideConPublicado() {
            List<AppEvent> eventos = new ArrayList<>();
            bus.subscribe(AppEvent.CITA_CANCELADA, (e, d) -> eventos.add(e));

            bus.publish(AppEvent.CITA_CANCELADA, null);

            assertThat(eventos).containsExactly(AppEvent.CITA_CANCELADA);
        }

        @Test
        @DisplayName("Un observer de un evento NO recibe publicaciones de otro evento")
        void observerDeOtroEvento_noRecibe() {
            List<Object> recibidos = new ArrayList<>();
            bus.subscribe(AppEvent.USUARIO_CREADO, (e, d) -> recibidos.add(d));

            bus.publish(AppEvent.CITA_AGENDADA, "irrelevante");

            assertThat(recibidos).isEmpty();
        }

        @Test
        @DisplayName("publish() sin observers no lanza excepción")
        void publicarSinObservers_noLanzaExcepcion() {
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                    () -> bus.publish(AppEvent.USUARIO_DESACTIVADO, "data"));
        }

        @Test
        @DisplayName("publish() con data null llega al observer como null")
        void publicarConDataNull_llegaNull() {
            List<Object> recibidos = new ArrayList<>();
            bus.subscribe(AppEvent.USUARIO_ACTUALIZADO, (e, d) -> recibidos.add(d));

            bus.publish(AppEvent.USUARIO_ACTUALIZADO, null);

            assertThat(recibidos).containsExactly((Object) null);
        }

        @Test
        @DisplayName("Publicar en distintos eventos no mezcla notificaciones")
        void publicarDistintosEventos_noMezcla() {
            List<String> logCreado   = new ArrayList<>();
            List<String> logCancelado = new ArrayList<>();

            bus.subscribe(AppEvent.USUARIO_CREADO,   (e, d) -> logCreado.add((String) d));
            bus.subscribe(AppEvent.CITA_CANCELADA,   (e, d) -> logCancelado.add((String) d));

            bus.publish(AppEvent.USUARIO_CREADO,  "A");
            bus.publish(AppEvent.CITA_CANCELADA,  "B");

            assertThat(logCreado).containsExactly("A");
            assertThat(logCancelado).containsExactly("B");
        }
    }

    // -----------------------------------------------------------------------
    // unsubscribe
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("unsubscribe()")
    class UnsubscribeTest {

        @Test
        @DisplayName("El observer desuscrito deja de recibir eventos")
        void observerDesuscrito_noRecibeEventos() {
            List<Object> recibidos = new ArrayList<>();
            Observer<String> obs = (e, d) -> recibidos.add(d);
            bus.subscribe(AppEvent.CITA_AGENDADA, obs);

            bus.unsubscribe(AppEvent.CITA_AGENDADA, obs);
            bus.publish(AppEvent.CITA_AGENDADA, "ignorar");

            assertThat(recibidos).isEmpty();
        }

        @Test
        @DisplayName("Desuscribir un observer no afecta a los demás del mismo evento")
        void desuscribirUno_noAfectaOtros() {
            List<Object> recibidosPor2 = new ArrayList<>();
            Observer<String> obs1 = (e, d) -> {};
            Observer<String> obs2 = (e, d) -> recibidosPor2.add(d);

            bus.subscribe(AppEvent.CITA_AGENDADA, obs1);
            bus.subscribe(AppEvent.CITA_AGENDADA, obs2);
            bus.unsubscribe(AppEvent.CITA_AGENDADA, obs1);

            bus.publish(AppEvent.CITA_AGENDADA, "llegó");

            assertThat(recibidosPor2).containsExactly("llegó");
        }

        @Test
        @DisplayName("unsubscribe() de un observer no registrado no lanza excepción")
        void desuscribirNoRegistrado_noLanzaExcepcion() {
            Observer<String> obs = (e, d) -> {};
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                    () -> bus.unsubscribe(AppEvent.CITA_AGENDADA, obs));
        }

        @Test
        @DisplayName("Observer puede suscribirse de nuevo tras desuscribirse")
        void resuscribir_funcionaCorrectamente() {
            List<Object> recibidos = new ArrayList<>();
            Observer<String> obs = (e, d) -> recibidos.add(d);

            bus.subscribe(AppEvent.USUARIO_CREADO, obs);
            bus.unsubscribe(AppEvent.USUARIO_CREADO, obs);
            bus.subscribe(AppEvent.USUARIO_CREADO, obs);
            bus.publish(AppEvent.USUARIO_CREADO, "re-suscrito");

            assertThat(recibidos).containsExactly("re-suscrito");
        }
    }

    // -----------------------------------------------------------------------
    // Copia defensiva (thread-safe durante notificación)
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("Copia defensiva durante notificación")
    class CopiaDeFensivaTest {

        @Test
        @DisplayName("Observer que se desuscribe durante la notificación no lanza ConcurrentModificationException")
        void desuscribirDurantePublicacion_noLanzaExcepcion() {
            Observer<?>[] ref = new Observer[1];
            ref[0] = (e, d) -> bus.unsubscribe(AppEvent.CITA_AGENDADA, ref[0]);

            bus.subscribe(AppEvent.CITA_AGENDADA, ref[0]);

            org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                    () -> bus.publish(AppEvent.CITA_AGENDADA, "data"));
        }
    }

    // -----------------------------------------------------------------------
    // AppEvent enum
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("AppEvent enum")
    class AppEventEnumTest {

        @Test
        @DisplayName("Contiene exactamente los seis eventos definidos")
        void contieneSeis_eventos() {
            assertThat(AppEvent.values()).hasSize(6);
        }

        @Test
        @DisplayName("Contiene todos los eventos esperados")
        void contieneEventosEsperados() {
            assertThat(AppEvent.values()).containsExactlyInAnyOrder(
                    AppEvent.USUARIO_CREADO,
                    AppEvent.USUARIO_ACTUALIZADO,
                    AppEvent.USUARIO_DESACTIVADO,
                    AppEvent.CITA_AGENDADA,
                    AppEvent.CITA_CANCELADA,
                    AppEvent.CITA_ACTUALIZADA
            );
        }
    }
}
