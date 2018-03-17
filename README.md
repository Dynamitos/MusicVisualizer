# MusicVisualizer

This is my own Music Visualizer. At first, it was planned to be a personal project, but now I have neither enough time now enough interest to finish it.

Use and modify this program as you want, I do not have any licence to prevent you from doing so. Instead, I hope someone will finish this project for me.

The idea behind it was to create a program, that easily generates a spectrum analyser to a song, like e.g. Teminite uses. If you don't know what I mean, here is an example: https://www.youtube.com/watch?v=qZufr9B0QzQ. Of course, this should be a little bit more customizable, but it should be easy to use. While developing, I added some of my own ideas like the option to let the image scale and brighten with the bass. After a while, I got the idea to add some sort of overlay, a second image, which is mostly transparent, which can hold the songs title or some lyrics. This is pretty much the current state. I will try to add some documentation to make the whole program more readable, but that will take some time. If you have any questions regarding the documentation, write me an e-mail to dynamitos15@gmail.com.

It is working on my machine (Windows 10) but I have not tested it on other machines. It uses OpenGL, Minim and JavaFX for libraries. In the first release I configured it as a Maven project, so it should download and unpack the dependencies automatically on the first build. If you want to develop it in eclipse, I recommend using the m2e plugin.

The program is pretty self-explanatory, so I won't include a tutorial on how to use it. The only thing worth noticing is, that you   will need a song to play (.mp3), a background picture to display (.png) and an "overlay image" (.png), which can for now be completely transparent. But as you can probably see, there are a few features that I wanted to implement, but now I don't have time to do so anymore. Those features are:

[ ] display lyrics on the overlay

[ ] edit and remove the visualization lines

[ ] add an option to record and encode the visualization as a .mp4

[ ] create a port for android

Feel free to clone and fork this project. If anything is not working, or you want to add your commit, write me an e-mail, since I will rarely look here.
