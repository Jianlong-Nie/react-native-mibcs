
# react-native-mibcs

## Getting started

`$ npm install react-native-mibcs --save`

### Mostly automatic installation

`$ react-native link react-native-mibcs`

### Manual installation


#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNMibcsPackage;` to the imports at the top of the file
  - Add `new RNMibcsPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-mibcs'
  	project(':react-native-mibcs').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-mibcs/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-mibcs')
  	```


## Usage
```javascript
import RNMibcs from 'react-native-mibcs';

// TODO: What to do with the module?
RNMibcs;
```
  