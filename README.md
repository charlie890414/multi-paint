# Muti-Paint
#### NCU Introduction to Computer Science Ⅱ Lab Final Project




### Setup
##### [JAVAFX 12](https://openjfx.io/)

##### Set  VM arguments
```
--module-path /path/to/javafx-sdk/lib --add-modules javafx.controls
```
##### Run paint.java



---



### Feature

- [ ] Line
	
	- [ ] Socket support
	- [ ] Animation
- [x] Circle
	
	- [x] Socket support
	- [ ] Animation
- [x] Pencil
	
	- [x] Socket support
	- [ ] Animation
- [x] Eraser
	
	- [x] Socket support
	- [ ] Animation
- [x] Square
	
	- [x] Socket support
	- [ ] Animation
- [x] Fill
	
	- [ ] Socket support
```Java
Platform.runLater(new Runnable() {
	@Override public void run() {
        //pass
    }
});
```
- [x] Undo
	
	- [x] Socket support
- [x] Redo
  
    - [x]  Socket support
- [ ]  Hollow & Solid
	
	- [ ]  Socket support

### Known bug

~~Server can't connect more than one client~~

Socket Fill tune out Exception

Sometimes line and eraser may be strange

Socket Close Exception may be clearly