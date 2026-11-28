<!-- translated-from: README.md @ ebe4848fbb14 -->
<div align="center">

<img src="../media/logo.png" alt="Aira" width="88" />

# Aira

**Un asistente de IA en tu móvil: dile lo que necesitas y usará tus apps y la web hasta dejarlo hecho.**

Trabaja en una pantalla en segundo plano que no ves, así que puedes seguir usando el móvil con normalidad.

<br/>
<a href="https://github.com/ganguydhrubo/Aira/releases/latest"><img src="https://img.shields.io/badge/Descargar%20APK-Android%2011%2B-2ea043?style=for-the-badge&logo=android&logoColor=white" alt="Descargar APK" /></a> <a href="https://github.com/ganguydhrubo/Aira"><img src="https://img.shields.io/github/stars/ganguydhrubo/Aira?style=for-the-badge&logo=github&label=Star&color=FFD700&logoColor=white" alt="Star this repo" /></a>

<a href="https://github.com/ganguydhrubo/Aira/releases"><img src="https://img.shields.io/github/downloads/ganguydhrubo/Aira/total?label=descargas&color=2ea043" alt="downloads" /></a>
<a href="https://github.com/ganguydhrubo/Aira/releases/latest"><img src="https://img.shields.io/github/v/release/ganguydhrubo/Aira?include_prereleases&label=versi%C3%B3n" alt="version" /></a>
<a href="../../LICENSE"><img src="https://img.shields.io/badge/license-Apache--2.0-lightgrey" alt="license" /></a>

<br/>

[English](../../README.md) · [日本語](README.ja.md) · [한국어](README.ko.md) · Español · [Türkçe](README.tr.md) · [Tiếng Việt](README.vi.md)

<sub>Esta versión en español es una traducción del original en chino simplificado. Si ves algo que suena raro o que no coincide con el original, te agradecemos que lo corrijas con un PR.</sub>

<br/>

</div>

## Por qué Aira

| | |
|---|---|
| 📱 **No te quita la pantalla** | Abre apps, toca, escribe y se desplaza en una pantalla aparte en segundo plano mientras tú usas el móvil como siempre; si quieres mirar, toca la vista en miniatura |
| 🧩 **Funciona con cualquier app** | Mira la pantalla y encuentra los botones como lo haría una persona: no necesita APIs de la app ni integraciones previas |
| 🌐 **Llámalo desde cualquier página web** | En el navegador integrado, di lo que quieres en la página que estás leyendo y trabajará ahí mismo; si tocas la página, se detiene |
| 🧠 **Mejora con el uso** | Recuerda las preferencias que le cuentas y convierte las tareas que completa en habilidades, para hacerlas más rápido y con más fiabilidad la próxima vez |
| 🔒 **Tus datos se quedan en tu móvil** | No hay ningún servidor de Aira; usa tu propia clave de API del modelo o inicia sesión con una cuenta de ChatGPT, xAI u OpenRouter |
| 🆓 **Código abierto y gratis** | Apache-2.0, todo el código es público |

## Empieza en tres pasos

1. **Instálalo**: descarga el APK desde [Releases](https://github.com/ganguydhrubo/Aira/releases/latest).
2. **Conecta un modelo**: en Ajustes › Modelo, elige un proveedor e introduce una clave de API, o simplemente inicia sesión con una cuenta ([modelos e inicio de sesión](../models.md)).
3. **Para que maneje apps del móvil**: sigue el asistente «Set up phone control» de los ajustes y empareja la depuración inalámbrica una sola vez ([guía de configuración](../setup-guide.md)). Si solo quieres chatear, navegar y trabajar con archivos, puedes saltarte este paso.

Después, prueba a decirle: «Pon alarmas mañana a las 9:40 y a las 10:00» o «Mira el tiempo cada mañana a las 8 y avísame solo si va a llover».

> 💡 ¿Tienes un ordenador a mano? Envía esta frase al agente de IA de tu ordenador y lo instalará todo por ti:<br/>
> `Sigue los pasos de https://github.com/ganguydhrubo/Aira/blob/main/docs/install-with-agent.md para instalar Aira en el móvil Android conectado por USB.`

## Qué puede hacer

| | |
|---|---|
| **Manejar apps** | Lee listas largas e historiales de chat, toca y escribe, comparte texto con las apps; los inicios de sesión, códigos de verificación y pagos te los deja a ti |
| **Navegar por la web** | Un navegador integrado de tipo escritorio que mantiene tus sesiones, lee páginas, descarga y sube archivos; marca su propio ritmo al pasar páginas y, si algo lo bloquea, te lo pasa |
| **Lo que tienes en pantalla** | Toca dos veces la cápsula flotante para enviarle la pantalla que estás viendo: «¿qué le respondo?», «apúntame esto» |
| **Horarios y condiciones** | «Recuérdamelo mañana a las 9», «haz esto todos los viernes»; se ejecuta a su hora en la conversación original |
| **Interrúmpelo o páralo cuando quieras** | Sigue hablándole mientras trabaja para añadir o corregir cosas (mantén pulsado un mensaje que aún no haya recogido para retirarlo o editarlo); al parar te dice hasta dónde llegó, y con «sigue» retoma desde ahí |
| **Mantén pulsado para hablar** | Mantén pulsado el cuadro de texto para dictar y corrige antes de enviar; mantén pulsada la cápsula flotante para hablar y suelta para enviarlo directamente. Usa el reconocimiento de voz del teléfono o, si no tiene, un modelo de voz de tu proveedor |
| **Archivos y cálculo** | Un sandbox de Linux por conversación ejecuta Python, Node y shell sobre los archivos que le envías (arm64) |
| **Desde tu ordenador** | Las herramientas de IA de tu ordenador pueden encargarle tareas a Aira mediante CLI + Skill o MCP ([usar Aira desde un ordenador](../computer.md)) |

Por defecto, Aira funciona en **modo Preguntar**: solo te consulta antes de gastar dinero (hacer pedidos, pagar) y todo lo demás lo hace sin más. También puedes cambiar al **modo Autónomo**. Consulta [Preguntar y Autónomo](../modes.md).

Para ver cómo funciona cada cosa y los detalles, consulta las [funciones en detalle](../features.md).

<details>
<summary>Apps y sitios web con conocimiento de uso integrado (los que no aparecen también funcionan)</summary>

La lista completa está en la sección [What it can do](../../README.md#what-it-can-do) del README en inglés y se actualiza sola a medida que cambia el conocimiento integrado.

</details>

<details>
<summary>Proveedores de modelos compatibles</summary>

Los proveedores predefinidos y los tipos de API a los que se puede conectar con «Custom» aparecen en la sección [What it can do](../../README.md#what-it-can-do) del README en inglés, que se actualiza sola a partir del código.

También puedes prescindir de la clave e iniciar sesión con una cuenta de ChatGPT (Plus / Pro), xAI / Grok u OpenRouter. Los pasos para añadir un modelo y lo que conviene saber de cada inicio de sesión están en [modelos e inicio de sesión](../models.md).

</details>

## En qué móviles funciona

| | Requisito |
|---|---|
| Chat, web, archivos | Android 11 o posterior |
| Manejar apps del móvil | Android 12 o posterior; **se recomienda Android 14+** (la pantalla en segundo plano tiene su propio foco, así que no interfiere cuando escribes en la pantalla principal) |
| Sandbox de Linux | Dispositivos arm64 |

¿No sabes si tu móvil sirve? Ejecuta Ajustes › Recuperación y reparación › **Autocomprobación de compatibilidad**: 12 comprobaciones en un minuto, y puedes [enviarnos el resultado](https://github.com/ganguydhrubo/Aira/issues/new?template=device-report.yml) con un toque.

## Privacidad y seguridad

- **No hay ningún servidor de Aira**: lo que dices y lo que hay en tu pantalla solo se envía al proveedor de modelos que tú configuras.
- Las claves de API y los recuerdos se guardan cifrados en el móvil; las conversaciones y los registros de ejecución se quedan en el dispositivo; para buscar actualizaciones solo se contacta con GitHub.
- Manejar automáticamente apps y sitios web de terceros puede ir en contra de sus condiciones de uso, así que decide tú. Para lo importante, usa el modo Preguntar y revisa los resultados. Consulta el [aviso legal](../disclaimer.md).

<details>
<summary><b>Preguntas frecuentes</b></summary>

**¿La depuración inalámbrica se desactiva sola?** Android la desactiva al cambiar de Wi-Fi o al reiniciar. Si se lo permites, Aira la vuelve a activar por su cuenta; si aun así no conecta, vuelve a «Set up phone control» y empareja de nuevo.

**¿No encuentras la depuración inalámbrica en tu móvil?** Algunos sistemas no la incluyen. En ese caso puedes iniciar el asistente del móvil desde un ordenador por USB; consulta la [guía de configuración](../setup-guide.md).

**¿Hay pantallas que no se pueden leer ni capturar?** Las pantallas de pago o de introducción de contraseñas bloquean las capturas, así que Aira te pide que te encargues tú.

**¿Puedo usar un modelo que se ejecuta en mi propio ordenador?** Sí: elige «Custom» como proveedor e introduce su dirección en tu red local.

**¿La app está en español?** Sí. La interfaz está en español, inglés, árabe, persa, chino simplificado, chino tradicional, japonés, coreano, portugués, ruso, indonesio y vietnamita, sigue el idioma del sistema y se puede cambiar en Ajustes › Idioma. Puedes hablarle a Aira en español sin problema. El conocimiento integrado sobre apps y sitios web está escrito en chino, y el modelo lo entiende sin dificultad.

**¿De dónde vienen las actualizaciones?** Solo de los [Releases](https://github.com/ganguydhrubo/Aira/releases) de este repositorio; también puedes actualizar desde la app en Acerca de Aira › Buscar actualizaciones.

Hay más respuestas en las [preguntas frecuentes](../faq.md).

</details>

## Comunidad y contribuciones

- **Documentación**: toda la documentación y un orden de lectura sugerido están en el [índice de documentación](../README.md)
- **Informa de un problema**: [abre un issue](https://github.com/ganguydhrubo/Aira/issues/new/choose); en la app, Acerca de Aira › Enviar comentarios permite adjuntar información de diagnóstico
- **Contribuye**: añade conocimiento de uso para una app o un sitio web (texto plano, sin programar) o contribuye con código; consulta la [guía de contribución](../../.github/CONTRIBUTING.md)

<p align="center">⭐ Si Aira te ahorra tiempo, dale una estrella para que más gente lo encuentre.</p>

## Colaboradores

Gracias a todas las personas que han participado en Aira:

<a href="https://github.com/ganguydhrubo/Aira/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=ganguydhrubo/Aira&max=100&columns=12" alt="Contributors" />
</a>

## Licencia

[Apache-2.0](../../LICENSE) · [NOTICE](../../NOTICE) · [Componentes de terceros](../../licenses/THIRD_PARTY_NOTICES.md)
