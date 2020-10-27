# Cordova Fastcam

**Android only**

This cordova plugin allows you to take videos and pictures in a nearly latency free way and gives you exact timestamps of the media.

_I've decided to make this plugin open source as part of the Hacktoberfest, as it may be useful to others, too_

## The problem

All existing camera plugins for Cordova (at least the ones I know of) induce a meaningful latency between the call of the method which starts the actual recording and the point in time it really was taken. For time sensitive applications, where you need to map video frames or images to other data, e.g. GPS, this latency is mostly not acceptable. Most of those libraries use Android's `MediaRecorder`, which is the cause of those latencies. Also, the timestamp Android is using internally for it's files, saved in the metadata, doesn't contain milliseconds information of the creation date. By leveraging the [CameraView](https://natario1.github.io/CameraView/) library and it's ability to take snapshots directly from the memory, the latency was reduced to a minimum.

## Usage

First, install the plugin using the Cordova CLI:

```bash
cordova plugin add https://github.com/scriptify/cordova-plugin-fastcam.git
```

To start the image/video taking process, call the `startCamera` method:

```javascript
const mediaFiles = await navigator.fastCamera.startCamera({
  mode: "SINGLE_PHOTO",
});
```

Have a look at the [API](#API) section to see the other available modes.

If you have an external GPS device with an USB Serial Port, you can connect it to your Android phone using an OTG Cable. Make sure you enable the NMEA `GGA` sentence.
To enable GPS, call the following method **before** you call `startCamera`.

```javascript
navigator.fastCamera.initGps({
  /**
   * Optional, defaults to 115200
   * */
  baudRate: 115200,
  onData: (position) => {
    // Here, you can display the current position
    // somewhere in the UI or do something else
    // with it
  },
  onError: (e) => {
    // Error while establishing connection
    // to USB Serial
  },
});
```

## API

```typescript
interface FastCamera {
  startCamera(p: StartCameraParams): Promise<ResultingFile[]>;
  initGps(p: InitGpsParams): void;
}

interface StartCameraParams {
  /**
   * Defaults to SINGLE_PHOTO.
   * - SINGLE_PHOTO = Take one picture and close the camera
   * - PHOTO_SERIES = Take a series of photos with a rate of 200ms and close the camera
   * - VIDEO = Take a video and close the camera
   */
  mode?: "PHOTO_SERIES" | "SINGLE_PHOTO" | "VIDEO";
  /**
   * Before taking photos or videos,
   * this timestamp can be set from
   * the outside to mark an arbitrary
   * point in time of another system.
   * The camera class will, from that point
   * on, start a timer and use this
   * time as a frame of reference for
   * video and picture timestamps.
   * This can be used to sync with
   * external devices like GPS.
   */
  clockSyncTimestamp?: number;
}

/**
 * An array of this type
 * is returned from
 * the 'startCamera' method
 */
interface ResultingFile {
  filePath: string;
  /**
   * For photos, this is
   * the exact timestamp the
   * image was taken, and for
   * videos, it's the timestamp
   * when the camera finished
   * capturing.
   */
  timestamp: number;
  fileType: "VIDEO" | "IMAGE";
  /**
   * If a GPS device was connected
   * using USB OTG, the position
   * will be saved here.
   */
  position?: GpsPosition;
}

interface InitGpsParams {
  baudRate?: number;
  onData: (position: GpsPosition) => void;
  onError?: (e: any) => void;
}

export enum GpsFixType {
  INVALID = 0,
  GPS = 1,
  DGPS = 2,
  PPS = 3,
  RTK_FIXED = 4,
  FLOAT_RTK = 5,
  ESTIMATED = 6,
  MANUAL = 7,
  SIMULATION = 8,
}

interface GpsPosition {
  altitude: number;
  dir: number;
  fixed: boolean;
  lat: number;
  lon: number;
  quality: GpsFixType;
  time: number;
  velocity: number;
}
```
