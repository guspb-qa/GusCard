package com.example.guscard

data class StoreInfo(
    val name: String,
    val category: String,
    val logoName: String? = null,
    val fullLogoName: String? = null,
    val searchTerms: List<String> = emptyList() // Альтернативные названия для поиска
)

object PopularStores {
    val stores = listOf(
        // --- Магазины с добавленными логотипами ---
        StoreInfo(
            "Пятёрочка", "Продукты", "pyaterochka_logo", "pyaterochka_full",
            searchTerms = listOf("Пятерочка", "Пятерка", "Pyaterochka")
        ),
        StoreInfo(
            "Лента", "Продукты", "lenta_logo", "lenta_full",
            searchTerms = listOf("Lenta")
        ),
        StoreInfo(
            "Лемана ПРО", "Дом/ремонт", "lemana_pro_logo", "lemana_pro_full",
            searchTerms = listOf("Леруа Мерлен", "Leroy Merlin", "Lemana")
        ),
        StoreInfo(
            "Fix Price", "Дом/ремонт", "fix_price_logo", "fix_price_full",
            searchTerms = listOf("Фикс Прайс", "Фикс", "Fix", "FixPrice")
        ),

        // --- Остальные магазины ---
        StoreInfo("Магнит", "Продукты", null, null, searchTerms = listOf("Magnit")),
        StoreInfo("Ашан", "Продукты", null, null, searchTerms = listOf("Auchan")),
        StoreInfo("Перекрёсток", "Продукты", null, null, searchTerms = listOf("Perekrestok")),
        StoreInfo("ВкусВилл", "Продукты", null, null, searchTerms = listOf("VkusVill")),
        StoreInfo("Дикси", "Продукты", null, null, searchTerms = listOf("Dixy")),
        StoreInfo("Ozon", "Шопинг", null, null, searchTerms = listOf("Озон")),
        StoreInfo("Wildberries", "Шопинг", null, null, searchTerms = listOf("Вайлдберриз", "WB")),
        StoreInfo("Спортмастер", "Шопинг", null, null, searchTerms = listOf("Sportmaster")),
        StoreInfo("М.Видео", "Электроника", null, null, searchTerms = listOf("МВидео", "MVideo")),
        StoreInfo("Эльдорадо", "Электроника", null, null, searchTerms = listOf("Eldorado")),
        StoreInfo("ДНС", "Электроника", null, null, searchTerms = listOf("DNS")),
        StoreInfo("OBI", "Дом/ремонт", null, null, searchTerms = listOf("ОБИ")),
        StoreInfo("Лукойл", "АЗС", null, null, searchTerms = listOf("Lukoil")),
        StoreInfo("Газпромнефть", "АЗС", null, null, searchTerms = listOf("Gazpromneft")),
        StoreInfo("Роснефть", "АЗС", null, null, searchTerms = listOf("Rosneft"))
    )

    // Умный поиск: ищем по основному названию ИЛИ по синонимам
    fun searchStores(query: String): List<StoreInfo> {
        if (query.isBlank()) return emptyList()
        return stores.filter { store ->
            store.name.contains(query, ignoreCase = true) ||
                    store.searchTerms.any { term -> term.contains(query, ignoreCase = true) }
        }
    }
}