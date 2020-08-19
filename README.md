# Davidson's AES Encryptor
## Description
A 256-bit AES encryptor written in Java with GUI built using the Swing framework. You can encrypt and decrypt files up to 150MB.

## File Formats
- Encrypted files have a `.encrypted` extension, and key files have a `.key` extension.
- Encrypting multiple files is currently not supported. However, you can encrypt a zip folder of these files.

## Downloads
##### Windows (64-bit): https://drive.google.com/file/d/1NGBbJ_xrZtUzfAvYQo7U7xp4ujKDF9hm/view?usp=sharing
##### MacOS (64-bit): https://drive.google.com/file/d/1wlpy8lLM1rmtC4JJH0Bl4mvnOXxKdLTb/view?usp=sharing
##### .jar File: https://drive.google.com/file/d/1rC_ZgJIxhSAwH8spKfzvDFYEir_dQzcp/view?usp=sharing

## Installation
##### Windows
1. Double-click the encryptor_windows-x64_1_0.exe file.
2. Proceed through the installer.
3. Run the newly created file.

##### MacOS
1. Double-click the encryptor_macos_1_0.dmg file.
2. You will get an error that it is blocked by Apple. This is because the app is not notarized by me and I don't currently have an Apple Developer ID to sign the code.
3. Navigate to System Preferences > Security & Privacy. Davidson's AES Encryptor will show up with an option to "Open Anyway".
4. Click "Open Anyway".
5. Proceed through the installer.
6. Run the newly created file.

##### JAR File
1. Alternatively, you can run the .jar file directly if you have Java 14.0 installed. [[Download Java]](https://www.oracle.com/java/technologies/javase-jdk14-downloads.html)
2. Download encryptor-1.0.jar from the link above.
3. Open your Terminal and navigate to the directory of the .jar file.
4. Run `java -jar encryptor-1.0.jar`.

## Acknowledgments
- Huge thanks to https://www.samiam.org/ for explaining the AES key-scheduling and mix-columns algorithms in depth. Wikipedia was helpful but this project would have taken countless more hours without this man's help.
- [Install4J](https://www.ej-technologies.com/products/install4j/overview.html) helped me package the JRE into the executable files for Windows and MacOS.
