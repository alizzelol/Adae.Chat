ADAE CHAT

Ésta es una aplicación que estoy llevando a cabo para la Asociación para el derecho a estudiar (ADAE), se trata de un chat privado que se llevaría a cabo entre los padres y los profesores de la asociación.
Lo que conlleva la aplicación:
- Registrarse para poder acceder a la aplicación
- Loguin para acceder a la aplicación
- Chat -> pantalla en la que se muestran las conversaciones activas y botón para abrir una nueva conversación
- Pantalla en la que llevar a cabo una conversación fluida.

La aplicación se lleva a cabo con Firestore Database, por lo que serán necesarias las siguientes dependecias en el build.gradle(:app):
implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
implementation("com.google.firebase:firebase-analytics")
implementation 'com.google.firebase:firebase-auth' 
implementation 'com.google.firebase:firebase-firestore:24.6.0'

Ahora mismo se trata de un proyecto en proceso, ésta sería solo una parte de lo que conllevaría la aplicación en su totalidad.
