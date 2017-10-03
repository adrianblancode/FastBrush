# FastBrush

FastBrush is a state of the art implementation for real time brush simulation for Android using OpenGL. It both achieves unprecedented detail of results and is lightweight enough to be implemented for mobile devices. It uses multidimensional data driven modeling to create a deformation table, which enables calculating the physics of the brush deformations in constant time for the entire brush, and thus the physics calculation overhead of a large number of bristles becomes negligible.

The final result of this system has far higher detail than available consumer painting applications. A paintbrush has up to a thousand bristles, while Adobe Photoshop is only able to simulate up to 10% of that amount in real-time, FastBrush is able to capture the full fidelity of a brush with up to a thousand bristles in real-time on consumer mobile devices.

This work is part of my master thesis in Computer Science at KTH Royal Institute of Technology, in Stockholm, Sweden.

[Simulating High Detail Brush Painting on Mobile Devices [PDF]](http://adrianblan.co/files/Simulating_High_Detail_Brush_Painting_on_Mobile_Devices.pdf)


## Media
![Photoshop Brush](http://i.imgur.com/vI5QqZZ.png)
Source: Adobe Photoshop CC 2015, using the maximum hundred bristles at the maximum size (300px)

![FastBrush Brush](http://i.imgur.com/VXJGO1Q.png)
Source: FastBrush, using a thousand bristles at the maximum size

![FastBrush Painting](http://i.imgur.com/1tkdxAY.png)
Source: Jeasmine Ljungström using FastBrush

![FastBrush Calligraphy](http://i.imgur.com/eB4sM7v.jpg)
Source: Huiting Wang using FastBrush

[FastBrush Demo Video](https://www.youtube.com/watch?v=gaym9G8vCZE)
Source: Huiting Wang using FastBrush
