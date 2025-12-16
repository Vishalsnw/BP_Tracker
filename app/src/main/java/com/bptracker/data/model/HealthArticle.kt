package com.bptracker.data.model

data class HealthArticle(
    val id: Int,
    val title: String,
    val summary: String,
    val content: String,
    val category: ArticleCategory,
    val imageRes: Int? = null
)

enum class ArticleCategory(val label: String) {
    DIET("Diet & Nutrition"),
    EXERCISE("Exercise & Fitness"),
    LIFESTYLE("Lifestyle"),
    MEDICATION("Medication"),
    STRESS("Stress Management"),
    UNDERSTANDING("Understanding BP")
}

object HealthArticlesData {
    val articles = listOf(
        HealthArticle(
            id = 1,
            title = "Understanding Blood Pressure Numbers",
            summary = "Learn what your blood pressure readings mean and why they matter.",
            content = """
                Blood pressure is measured using two numbers:
                
                **Systolic Pressure (Top Number)**
                This measures the pressure in your arteries when your heart beats and pumps blood.
                
                **Diastolic Pressure (Bottom Number)**
                This measures the pressure in your arteries when your heart rests between beats.
                
                **Blood Pressure Categories:**
                
                • **Normal**: Less than 120/80 mmHg
                • **Elevated**: Systolic 120-129 and diastolic less than 80
                • **High Blood Pressure Stage 1**: Systolic 130-139 or diastolic 80-89
                • **High Blood Pressure Stage 2**: Systolic 140+ or diastolic 90+
                • **Hypertensive Crisis**: Systolic over 180 and/or diastolic over 120
                
                Regular monitoring helps you and your doctor track your heart health over time.
            """.trimIndent(),
            category = ArticleCategory.UNDERSTANDING
        ),
        HealthArticle(
            id = 2,
            title = "The DASH Diet for Lower Blood Pressure",
            summary = "Discover how the DASH diet can help reduce your blood pressure naturally.",
            content = """
                The DASH (Dietary Approaches to Stop Hypertension) diet is a proven way to lower blood pressure.
                
                **Key Principles:**
                
                • **Eat more fruits and vegetables** - Aim for 4-5 servings each daily
                • **Choose whole grains** - At least 6 servings daily
                • **Include lean proteins** - Fish, poultry, and legumes
                • **Limit sodium** - Keep under 2,300mg daily (ideally 1,500mg)
                • **Reduce saturated fats** - Choose low-fat dairy products
                • **Limit sweets** - Reduce sugar-sweetened beverages
                
                **Foods to Enjoy:**
                - Bananas, oranges, and berries
                - Leafy greens like spinach and kale
                - Nuts and seeds
                - Oatmeal and brown rice
                - Salmon and other fatty fish
                
                Studies show the DASH diet can lower blood pressure within two weeks!
            """.trimIndent(),
            category = ArticleCategory.DIET
        ),
        HealthArticle(
            id = 3,
            title = "Exercise Tips for Heart Health",
            summary = "Learn the best exercises to help manage your blood pressure.",
            content = """
                Regular physical activity is one of the most effective ways to control blood pressure.
                
                **Recommended Activities:**
                
                • **Aerobic Exercise** - Walking, swimming, cycling, or dancing for 30 minutes most days
                • **Strength Training** - Light weights 2-3 times per week
                • **Flexibility** - Stretching and yoga for stress relief
                
                **Getting Started:**
                
                1. Start slowly - even 10 minutes of walking helps
                2. Gradually increase duration and intensity
                3. Find activities you enjoy
                4. Set realistic goals
                5. Track your progress
                
                **Safety Tips:**
                
                - Consult your doctor before starting
                - Warm up before and cool down after
                - Stay hydrated
                - Stop if you feel dizzy or short of breath
                
                Aim for at least 150 minutes of moderate activity per week.
            """.trimIndent(),
            category = ArticleCategory.EXERCISE
        ),
        HealthArticle(
            id = 4,
            title = "Managing Stress for Better Blood Pressure",
            summary = "Stress can raise blood pressure. Learn techniques to stay calm and healthy.",
            content = """
                Chronic stress contributes to high blood pressure. Managing stress is essential for heart health.
                
                **Stress Reduction Techniques:**
                
                **Deep Breathing**
                Practice 4-7-8 breathing: Inhale for 4 seconds, hold for 7, exhale for 8.
                
                **Meditation**
                Even 5-10 minutes of daily meditation can help lower blood pressure.
                
                **Progressive Muscle Relaxation**
                Tense and release muscle groups from head to toe.
                
                **Lifestyle Changes:**
                
                • Get 7-9 hours of quality sleep
                • Limit caffeine and alcohol
                • Take breaks during work
                • Spend time in nature
                • Connect with loved ones
                • Practice gratitude daily
                
                **When to Seek Help:**
                
                If stress feels overwhelming, consider speaking with a mental health professional.
            """.trimIndent(),
            category = ArticleCategory.STRESS
        ),
        HealthArticle(
            id = 5,
            title = "Reducing Sodium in Your Diet",
            summary = "Simple tips to cut salt and improve your blood pressure.",
            content = """
                High sodium intake is linked to high blood pressure. Most people consume too much salt.
                
                **Daily Sodium Goals:**
                - Ideal: 1,500 mg or less
                - Maximum: 2,300 mg (about 1 teaspoon of salt)
                
                **Tips to Reduce Sodium:**
                
                1. **Read nutrition labels** - Compare products and choose lower sodium options
                2. **Cook at home** - You control the salt
                3. **Use herbs and spices** - Flavor without sodium
                4. **Rinse canned foods** - Reduces sodium by up to 40%
                5. **Choose fresh foods** - Avoid processed and packaged items
                6. **Limit condiments** - Soy sauce, ketchup, and dressings are high in sodium
                
                **Hidden Sodium Sources:**
                - Bread and rolls
                - Pizza
                - Sandwiches
                - Cold cuts and deli meats
                - Soup
                - Cheese
                
                Your taste for salt will decrease over time as you reduce intake.
            """.trimIndent(),
            category = ArticleCategory.DIET
        ),
        HealthArticle(
            id = 6,
            title = "How to Measure Blood Pressure Correctly",
            summary = "Get accurate readings by following these measurement guidelines.",
            content = """
                Accurate blood pressure measurements are essential for monitoring your health.
                
                **Before Measuring:**
                
                • Don't smoke, exercise, or drink caffeine 30 minutes before
                • Use the bathroom first
                • Rest quietly for 5 minutes before measuring
                • Sit with back supported and feet flat on floor
                • Remove tight sleeves
                
                **During Measurement:**
                
                • Place arm on flat surface at heart level
                • Position cuff on bare skin, 1 inch above elbow
                • Keep legs uncrossed
                • Don't talk during measurement
                • Take 2-3 readings, 1 minute apart
                
                **Tips for Accuracy:**
                
                - Use the same arm each time
                - Measure at the same time daily
                - Record all readings
                - Note any unusual circumstances
                
                **When to Measure:**
                
                Morning (before medications) and evening are ideal times. Consistency is key!
            """.trimIndent(),
            category = ArticleCategory.UNDERSTANDING
        ),
        HealthArticle(
            id = 7,
            title = "Lifestyle Changes That Lower Blood Pressure",
            summary = "Simple daily habits that can make a big difference.",
            content = """
                Small lifestyle changes can have a significant impact on blood pressure.
                
                **Key Changes:**
                
                **Maintain Healthy Weight**
                Losing just 10 pounds can lower blood pressure. Focus on gradual, sustainable weight loss.
                
                **Limit Alcohol**
                - Men: No more than 2 drinks per day
                - Women: No more than 1 drink per day
                
                **Quit Smoking**
                Smoking raises blood pressure and damages blood vessels. Benefits start immediately after quitting.
                
                **Get Quality Sleep**
                Poor sleep is linked to higher blood pressure. Aim for 7-9 hours nightly.
                
                **Reduce Caffeine**
                Caffeine can temporarily raise blood pressure. Limit to 2-3 cups of coffee daily.
                
                **Monitor Regularly**
                Track your blood pressure to see how lifestyle changes affect your readings.
                
                **Impact of Changes:**
                
                Each change can lower systolic pressure by 4-11 mmHg. Combined, the effects are even greater!
            """.trimIndent(),
            category = ArticleCategory.LIFESTYLE
        ),
        HealthArticle(
            id = 8,
            title = "Understanding Blood Pressure Medications",
            summary = "Learn about common medications used to treat high blood pressure.",
            content = """
                Sometimes lifestyle changes alone aren't enough, and medication is needed.
                
                **Common Medication Types:**
                
                **Diuretics (Water Pills)**
                Help kidneys remove excess sodium and water.
                
                **ACE Inhibitors**
                Relax blood vessels by blocking a hormone that narrows them.
                
                **Calcium Channel Blockers**
                Prevent calcium from entering heart and blood vessel cells.
                
                **Beta Blockers**
                Reduce heart rate and the heart's workload.
                
                **ARBs**
                Similar to ACE inhibitors, help relax blood vessels.
                
                **Important Medication Tips:**
                
                • Take medications exactly as prescribed
                • Don't stop suddenly without doctor's advice
                • Report side effects to your doctor
                • Keep a list of all medications
                • Refill prescriptions before running out
                
                **Remember:**
                This is general information. Always consult your healthcare provider about your specific medications.
            """.trimIndent(),
            category = ArticleCategory.MEDICATION
        )
    )
}
