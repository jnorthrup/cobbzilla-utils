package org.cobbzilla.util.javascript

import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import lombok.experimental.Accessors

@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
class JsEngineConfig {

    @Getter
    @Setter
    val minEngines: Int = 0
    @Getter
    @Setter
    val maxEngines: Int = 0
    @Getter
    @Setter
    val defaultScript: String? = null

}
