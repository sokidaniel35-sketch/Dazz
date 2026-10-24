# Dazz 1.0.6 — Target SDK 37 + Camera Extensions (vendor extensions)

> Estado: alterações no CÓDIGO-FONTE. NÃO foram compiladas nem testadas (sem Flutter/Gradle/rede no ambiente de edição).
> O APK precisa ser gerado por você (flutter build apk) ou pelo workflow do GitHub.

## O que mudou
1. `flutter_app/android/app/build.gradle`
   - `targetSdk` 36 → 37 (`compileSdk` mantido em 36, ver "compileSdk 37" abaixo).
   - CameraX 1.4.1 → 1.5.2 (versão centralizada em `camerax_version`; removida a linha duplicada de camera-camera2).
2. `camera/CameraExtensionsHelper.kt` (novo): consulta Camera2 (`CameraExtensionCharacteristics`, API 31+) e mapeia modos.
   IDs desconhecidos aparecem como `vendor_<id>` (não sei a numeração das extensões do Android 17: o app só mostra o que o aparelho anuncia).
3. `camera/CameraPlugin.kt`
   - Novos métodos do canal: `getExtensionSupport` e `setExtensionMode(mode)` (none, auto, hdr, night, bokeh, face_retouch).
   - `ExtensionsManager` é criado sob demanda (não altera o tempo de abertura do app).
   - Com extensão ativa: só Preview + ImageCapture (CameraX não permite VideoCapture junto), sem JPEG_QUALITY/EDGE_MODE via Camera2Interop.
   - Fallback automático para "none" se a extensão não existir na câmera, o bind falhar ou ao trocar para a frontal (evento `onExtensionModeChanged`).
   - Gravação de vídeo com extensão ativa retorna `success:false, reason:extension_active`.
   - `releaseCamera` reseta a extensão.
4. Dart: `constants.dart`, `camera_service.dart` (estado `extensionMode`/`extensionSupport`, métodos `getExtensionSupport`/`setExtensionMode`), `camera_screen.dart` (chip âmbar de teste abaixo do overlay de debug).

## Como testar no A56
1. Gere e instale o APK. Abra o app → menu superior → ícone de debug (bug) para ligar o overlay.
2. Toque no chip âmbar "EXT": a 1ª vez consulta o suporte e mostra `CameraX[...]` e `Camera2[...]`.
   Se as listas vierem vazias, o aparelho não expõe extensões para terceiros nessa câmera — nada a corrigir no app.
3. Toque de novo para alternar none → cada modo disponível. Teste foto em cada modo e a gravação de vídeo (deve ficar indisponível com extensão).
4. Logcat: `adb logcat -s CameraPlugin CameraExtensions` (procure "extension fallback").

## Pontos de atenção (não verificados por mim)
- Efeito de filme: com extensão, o frame vem pós-processado pelo fabricante (HDR/noite) e pode alterar a estética dos filtros.
- Latência de captura em modo Night é maior por natureza; não medi nada.
- CameraX 1.5.2 é salto de minor: pode haver avisos/depreciações na compilação.
- Target 37: orientação/aspect-ratio travados são ignorados em telas >= 600dp (tablet/dobrável), não no A56. Plugins antigos
  (share_plus 7, permission_handler 11, go_router 13…) podem usar reflexão/campos `static final` — rode `flutter pub upgrade` e teste.

## compileSdk 37 (não aplicado)
Fontes públicas indicam AGP >= 9.1 e Gradle >= 9.3 para compilar com API 37; o projeto está em AGP 8.9.1 / Gradle 8.11.1.
Mantive compileSdk 36 para não quebrar o build do Flutter; o comportamento de runtime do target 37 já vale com `targetSdk = 37`.
