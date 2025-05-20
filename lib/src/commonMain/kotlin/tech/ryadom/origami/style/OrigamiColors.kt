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

import androidx.compose.ui.graphics.Color

/**
 * Origami colors
 *
 * @property backgroundColor background [Color]
 * @property guidelinesColor guidelines [Color]
 * @property edgesColor a [Color] of guideline's edges
 */
interface OrigamiColors {
    val backgroundColor: Color

    val guidelinesColor: Color

    val edgesColor: Color

    companion object {
        /**
         * Default values for [OrigamiColors]
         */
        fun createDefault() = object : OrigamiColors {
            override val backgroundColor: Color = Color.Black.copy(alpha = 0.5F)

            override val guidelinesColor: Color = Color.Gray

            override val edgesColor: Color = guidelinesColor
        }
    }
}
