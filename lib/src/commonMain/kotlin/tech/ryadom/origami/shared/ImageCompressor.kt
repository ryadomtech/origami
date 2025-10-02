/*
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
*/

package tech.ryadom.origami.shared

import androidx.compose.ui.graphics.ImageBitmap
import tech.ryadom.origami.style.OrigamiCompression

interface ImageCompressor {

    fun scaleToPlatformLimits(image: ImageBitmap): ImageBitmap

    suspend fun compress(image: ImageBitmap, compression: OrigamiCompression): ImageBitmap

    class Original : ImageCompressor {
        override fun scaleToPlatformLimits(image: ImageBitmap): ImageBitmap {
            return image
        }

        override suspend fun compress(
            image: ImageBitmap,
            compression: OrigamiCompression
        ): ImageBitmap {
            return image
        }
    }
}

expect fun createImageCompressor(): ImageCompressor