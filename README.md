# Occultus-Server - A Server for the Occultus PGP-Messenger Client
This server application is the result of my PGP-messenger school project. I
developed a small Java-based [client](https://www.github.com/Gregor-Gottschewski/Occultus-Messenger)
and this server.
The client is the main part of my project. It can decrypt, encrypt and send messages.
The server stores messages and user data but cannot encrypt or decrypt any data.

> Because the focus of my school project was encryption, I spent less time
> developing this server. The result: this server is not capable to handle HTTP
> requests. It only works with the Occultus client.
>
> However, I am working to make the server capable to handle HTTP requests.

## Important disclaimers
1. The client software uses cryptographic software.
   Some countries restrict the use and/or export of this type of software.
   If you are potentially subject to such restrictions, you should seek legal advice before attempting to develop or
   distribute cryptographic code. **This could also affect this server software.**
2. This software should not be used for safe communication, not even for any other type of communication.
   It is a school project and its purpose is to demonstrate PGP in communication.

## Install Occultus-Server
### Prerequisites
The following software must be installed on your system:
- [x] **MySQL** - MySQL must be installed, set up and running
- [x] **JDK 17**

### Installation

Download the server .jar [here](https://github.com/Gregor-Gottschewski/Occultus-Server/releases).
To start the server enter:

      java -jar occultusserver0.0.1.jar

You should see a menu. Enter 'C', press enter and follow the instructions.
After the installation, the server runs.

### Run the server
Start Occultus-Server via your terminal:

      java -jar occultus-server0.0.1.jar --port 2999 --password <your_occultus_password>

**The default port is 2100.**

## Client Software
The client software can be found here: https://www.github.com/gregor-gottschewski/occultus-messenger.

## Help
You are welcome to ask questions, share issues and contribute to this project. Here are some solutions for
problems that could occur while using Occultus-Server.

### Error: Password policy requirements not satisfied

      Your password does not satisfy the current policy requirements

Please set a password that matches your MySQL
[password policy](https://dev.mysql.com/doc/refman/8.0/en/validate-password-options-variables.html)
or [uninstall the Password Validation Component](https://dev.mysql.com/doc/refman/8.0/en/validate-password-installation.html) (not recommended).
### I cannot connect to my database
Please check the following things:
1. **Check your password and try again.**
2. **Is MySQL running?**

   Type `systemctl is-active mysql.service` in your terminal.
3. **Does the Occultus database exists?**

   Run `SHOW DATABASES;` in MySQL.
4. **Does the Occultus-Server database user exists?**

   Type `SELECT user FROM mysql.user WHERE user="occultus-user";` in MySQL.

If that does not solve your issue, delete the database by pressing 'D' in the start-menu
(all occultus-database data will be lost).

## License
> MIT License
>
> Copyright (c) 2023 Gregor Gottschewski
>
> Permission is hereby granted, free of charge, to any person obtaining a copy
> of this software and associated documentation files (the "Software"), to deal
> in the Software without restriction, including without limitation the rights
> to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
> copies of the Software, and to permit persons to whom the Software is
> furnished to do so, subject to the following conditions:
>
> The above copyright notice and this permission notice shall be included in all
> copies or substantial portions of the Software.
>
> THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
> IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
> FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
> AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
> LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
> OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
>SOFTWARE.