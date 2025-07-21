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

package tech.ryadom.origami.style

import androidx.compose.ui.util.fastCoerceIn

/**
 * Padding between [OrigamiCropArea] and source
 */
interface OrigamiCropAreaPadding {

    /**
     * Returns padding factor
     * @param s max available side size
     */
    fun getFactor(s: Float): Float

    companion object {
        fun createDefault(): OrigamiCropAreaPadding {
            return FactorPadding(0.15f)
        }
    }
}

class FactorPadding(
    private val factor: Float
) : OrigamiCropAreaPadding {
    override fun getFactor(s: Float): Float {
        return s * (1 - factor)
    }
}

class FixedPadding(
    private val width: Float
) : OrigamiCropAreaPadding {
    override fun getFactor(s: Float): Float {
        return (s - width).fastCoerceIn(0f, s)
    }
}