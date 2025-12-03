package com.example.pixelassistant

data class ModelCard(
    val id: String,
    val displayName: String,
    val quantization: String,
    val sizeGb: Double,
    val contextLength: Int,
    val description: String,
)

object ModelCatalog {
    val featuredModels = listOf(
        ModelCard(
            id = "litert-community/gemma-2b-it-int4",
            displayName = "Gemma 2B (int4)",
            quantization = "int4",
            sizeGb = 1.6,
            contextLength = 4096,
            description = "Fast multilingual chat tuned for low-power devices.",
        ),
        ModelCard(
            id = "litert-community/phi-3-mini-4k-instruct-int4",
            displayName = "Phi-3 Mini (4K)",
            quantization = "int4",
            sizeGb = 2.4,
            contextLength = 4096,
            description = "Great for reasoning and tool-use style prompts.",
        ),
        ModelCard(
            id = "litert-community/llama-3-8b-instruct-int4",
            displayName = "Llama 3 8B (int4)",
            quantization = "int4",
            sizeGb = 5.4,
            contextLength = 8192,
            description = "Highest quality response option for the Pixel 10 Pro XL.",
        ),
    )
}
