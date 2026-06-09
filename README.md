# 📰 Kriba - Plataforma de Noticias Personalizada

---

## 🎯 1. Objetivo del Proyecto

> Desarrollar una aplicación web basada en una arquitectura de **microservicios** que ofrezca a los usuarios un feed de noticias dinámico y altamente personalizado. El sistema aprenderá de las interacciones del usuario y combinará sus suscripciones explícitas con un **motor de recomendaciones inteligente**.

---

## 💻 2. Stack Tecnológico

* **Frontend:** ![Angular](https://img.shields.io/badge/Angular-DD0031?style=flat-square&logo=angular&logoColor=white) (diseño reactivo, infinite scroll, consumo eficiente de APIs).
* **Backend:** ![Spring Boot](https://img.shields.io/badge/Java_Spring_Boot_4-6DB33F?style=flat-square&logo=spring-boot&logoColor=white).
* **Seguridad:** ![JWT](https://img.shields.io/badge/JWT-000000?style=flat-square&logo=json-web-tokens&logoColor=white) Autenticación stateless mediante JWT y delegación ![OAuth2](https://img.shields.io/badge/OAuth2-EB5424?style=flat-square&logo=auth0&logoColor=white) (ej. Google) gestionada por el backend.
* **Persistencia:** ![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white).
* **Caché, Concurrencia y Resiliencia:** ![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white) (vía Spring Data Redis) y **Resilience4j**.
* **Integraciones:** **GNews API**, **JSoup** (Web Scraping) y **Gemini 3.1 Flash** (procesamiento LLM ultrarrápido).
* **Infraestructura y Despliegue:** Despliegue continuo (**CI/CD**) en servidor privado utilizando ![Dokploy](https://img.shields.io/badge/Dokploy-6366F1?style=flat-square) para la gestión automatizada de contenedores (aplicación web, base de datos relacional y nodo de Redis).

---

## ✨ 3. Funcionalidades Clave

* **🗂️ Feed Inteligente:** muro de noticias que mezcla artículos de los periódicos a los que el usuario está suscrito junto con recomendaciones basadas en sus hábitos de lectura.
* **🤖 Resúmenes con IA "Bajo Demanda" (Extracción Dinámica):** botón interactivo en artículos extensos. El sistema navega a la fuente original, extrae el texto real mediante scraping y genera un resumen de 2-3 párrafos utilizando un LLM. Limitado a un número de usos diarios por usuario.
* **⭐ Suscripciones e Interacciones:** sistema relacional directo para suscripción a fuentes concretas y guardado de "Favoritos".

---

## 🧠 4. Procesos Complejos / Valor Añadido

### A. Patrón Agregador Concurrente
Este proceso entra en acción en el momento exacto en que el usuario solicita su feed. Para no penalizar el tiempo de respuesta, el **módulo orquestador** utiliza **programación multihilo no bloqueante** para consultar múltiples silos de datos de forma simultánea: lee el perfil del usuario en BD, obtiene sus preferencias y descarga las noticias (de caché o API). Actúa como un **"ensamblador"** que, en milisegundos, cruza las respuestas, elimina duplicados, ordena los artículos por relevancia y entrega el resultado final al frontend.

### B. Motor de Aprendizaje Asíncrono (Arquitectura Orientada a Eventos)
A diferencia del orquestador (que debe responder al instante), este proceso gestiona el trabajo analítico pesado "en la sombra" utilizando **Spring Application Events**. El sistema dispara eventos no bloqueantes al recopilar las interacciones del usuario (clics, favoritos, uso de IA), persistiendo el rastro sin afectar la navegación. Posteriormente, un proceso **Batch programado (`@Scheduled`)** analiza todo ese volumen de interacciones en diferido para recalcular matemáticamente y actualizar los pesos de las preferencias de cada perfil.

### C. Resiliencia y Consumo Inteligente de APIs Externas
El sistema contará con una capa de **caché distribuida de alto rendimiento (Redis)** para optimizar drásticamente los tiempos de respuesta y evitar exceder los límites de peticiones del proveedor de noticias (rate limiting). Al estar externalizada, la caché sobrevive a reinicios del servidor y prepara la aplicación para un futuro escalado horizontal. Además, se implementará **tolerancia a fallos** (mediante el patrón **Circuit Breaker / Fallback**). Si la extracción de texto con JSoup o la API de IA sufren caídas, antibots o latencias altas, el sistema ofrecerá una respuesta degradada elegante (usando el fragmento corto de la noticia) sin colapsar el hilo de ejecución principal.

### D. Estadísticas de Lectura
Un **panel visual** en el perfil del usuario que muestre sus tendencias e intereses, basándose en los datos precalculados por el motor de recomendaciones.

### E. Lista "Leer más tarde"
Una sección para guardar artículos mediante **marcadores (bookmarks)**, facilitando la lectura diferida sin alterar el algoritmo a largo plazo.
