# Pförtner - The digital door label

<p align="center">
  <img width="500" src="./Graphics/logo.png">
</p>
<p align="center">
  <img width="500" src="./Graphics/finish.png">
</p>

Pförtner is a fully digital door label.<br>
It utilizes a tablet outside of a office room to display information about the offices occupants.
The occupants inside the office can control the door label via applications on their smart phone.

Each occupant can display their current status and their office hours along with a picture.

A status for the whole room can be set along with a name for the room.

Additional Pförtner utilizes the tablets camera from the smartphone application to take a peek at who is outside of the door.

People outside the office can use Pförtner to arrange appointments. The selected occupant will get a notification on their smart phone and should the occupant accept the appointment will be added to their google calendar. While the creator of the appointments gets an email.

<p align="center">
  <a href="https://youtu.be/asGR4Xj08gg">
    Watch a trailer here.
  </a>
</p>


## Architecture

The following picture shows the communication between the different components of Pförtner.
<p align="center">
  <img width="500" src="./graphics/arch.png">
</p>



For a detailed overview of Pförtners architecture take a look at the [technical documentation](Pfoertner-Technische_Dokumentation.pdf).

## System Requirements

* At least 2 Android devices with Android 8.0 or higher.
* The recommended configuration is 1 tablet as the door label and 1 smartphone per person in working in the office.
* TODO: was war nochmal die max anzahl? 4?

## Setup instructions

Android studio can create the .apks for the clients from their corresponding projects: [PfoertnerAdmin](PfoertnerAdmin/) for the smart phone and [PfoertnerPanel](PfoertnerPanel/) for the table\label.

Both clients need the address where the server is hosted before the .apks are created.
For this [Config.java](PfoertnerCommon/src/main/java/de/tu_darmstadt/epool/pfoertner/common/Config.java) needs to be edited.

For the server simple follow the instructions in [PfoertnerServer](PfoertnerServer/)


## Credits
This Project was developed as part of the <em>Internet Praktikum Telekooperaion</em> at Technische Universität Darmstadt.

* Marc Arnold - [@m-arnold](https://github.com/m-arnold)
* Jonas Belouadi - [@potamides](https://github.com/potamides)
* Anton Haubner - [@ahbnr](https://github.com/ahbnr)
* David Heck - [@heckstrahler](https://github.com/heckstrahler)
* Martin Kerscher - [@maruker](https://github.com/maruker)

### Used Libraries
* [Node.js](https://nodejs.org/en/)
* [RxJava](https://github.com/ReactiveX/RxJava)
* [SQLite](https://sqlite.org/download.html)
* [Express](https://expressjs.com/)
* [Firebase](https://firebase.google.com/docs/cloud-messaging/)
* [Sequelize](http://docs.sequelizejs.com/)
* [Retrofit](http://square.github.io/retrofit/)
* [Room Persistence Library](https://developer.android.com/topic/libraries/architecture/room)
* [LivaData Library](https://developer.android.com/topic/libraries/architecture/livedata)

## License
TODO: gute frage ich meine wir hatte gpl gesagt, aber 
