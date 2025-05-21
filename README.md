# Origami

With this tool you can crop and perform some transformation actions (`rotate`, `flip`) on images in
Compose Multiplatform.

## Supported targets

| Target          | Implemented | Tested |
|-----------------|-------------|--------|
| **Android**     | ☑           | ☑      |
| **iOS**         | ☑           | ☑      |
| **JVM Desktop** | ☑           | ☑      |
| **JS**          | ☑           | ☑      |
| **WasmJS**      | ☑           | ☑      |

### Implementation

In your shared module's build.gradle.kts add

```Gradle Kotlin DSL
kotlin.sourceSets.commonMain.dependencies {
  implementation("tech.ryadom:origami:0.0.3")
}
```

### Usage

To create an `Origami` instance, you must call the `Origami.of()` funcation and pass an
`ImageBitmap` or `Painter` as an arguments.
Then use the `Origami` object as shown below.

```Kotlin
val source = createYourSource()
val origami = Origami.of(source)

OrigamiImage(
  origami = origami,
  colors = object: OrigamiColors { }, // Customize colors 
  cropArea = OrigamiCropArea() // Customize crop area
)

// Returns cropped image
origami.crop()
```

### License

```
   Copyright 2025 Ryadom Tech

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```

### Support

If you find a bug or want to contribute an improvement, please create an Issue or send an email to
opensource@ryadom.tech.
Any support will be appreciated.
