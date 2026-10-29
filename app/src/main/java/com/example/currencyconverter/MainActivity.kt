package com.example.currencyconverter

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.currencyconverter.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // View Binding : accès aux vues de activity_main.xml sans findViewById
    private lateinit var binding: ActivityMainBinding

    // Bonus : ViewModel pour garder le montant, le résultat et les devises
    private val viewModel: ConverterViewModel by viewModels()

    // Taux de change fixes pour 1 EUR
    private val taux = mapOf(
        "EUR" to 1.0, "USD" to 1.17, "TND" to 3.40,
        "GBP" to 0.87, "JPY" to 172.50, "CAD" to 1.62
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // On réaffiche les valeurs gardées par le ViewModel
        binding.tvMontant.text = viewModel.montant
        binding.tvResultat.text = viewModel.resultat

        remplirListes()
        brancherClavier()

        // Bouton Convertir
        binding.btnConvertir.setOnClickListener {
            convertirMontant(true)
        }

        // Bouton inverser ⇅
        binding.btnInverser.setOnClickListener {
            inverserDevises()
        }
    }

    // ---------- 4. Remplissage des deux Spinner ----------
    private fun remplirListes() {
        // ArrayAdapter construit à partir de R.array.devises (strings.xml)
        val adapterSource = ArrayAdapter.createFromResource(this, R.array.devises, R.layout.item_devise)
        adapterSource.setDropDownViewResource(R.layout.item_devise_liste)
        binding.spDeviseSource.adapter = adapterSource

        val adapterCible = ArrayAdapter.createFromResource(this, R.array.devises, R.layout.item_devise)
        adapterCible.setDropDownViewResource(R.layout.item_devise_liste)
        binding.spDeviseCible.adapter = adapterCible

        // Par défaut : source = USD (position 0), cible = EUR (position 1)
        if (viewModel.positionSource == -1) {
            viewModel.positionSource = 0
            viewModel.positionCible = 1
        }
        binding.spDeviseSource.setSelection(viewModel.positionSource)
        binding.spDeviseCible.setSelection(viewModel.positionCible)

        // Bonus : conversion automatique quand on change de devise
        val ecouteur = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.positionSource = binding.spDeviseSource.selectedItemPosition
                viewModel.positionCible = binding.spDeviseCible.selectedItemPosition
                convertirMontant(false)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // rien à faire
            }
        }
        binding.spDeviseSource.onItemSelectedListener = ecouteur
        binding.spDeviseCible.onItemSelectedListener = ecouteur
    }

    // ---------- 5. Clavier numérique ----------
    private fun brancherClavier() {
        binding.btn0.setOnClickListener { ajouterCaractere("0") }
        binding.btn1.setOnClickListener { ajouterCaractere("1") }
        binding.btn2.setOnClickListener { ajouterCaractere("2") }
        binding.btn3.setOnClickListener { ajouterCaractere("3") }
        binding.btn4.setOnClickListener { ajouterCaractere("4") }
        binding.btn5.setOnClickListener { ajouterCaractere("5") }
        binding.btn6.setOnClickListener { ajouterCaractere("6") }
        binding.btn7.setOnClickListener { ajouterCaractere("7") }
        binding.btn8.setOnClickListener { ajouterCaractere("8") }
        binding.btn9.setOnClickListener { ajouterCaractere("9") }
        binding.btnPoint.setOnClickListener { ajouterCaractere(".") }

        // Touche C : montant à 0 et résultat vide
        binding.btnEffacer.setOnClickListener {
            changerMontant("0")
            changerResultat("")
        }
    }

    // Ajoute un chiffre ou un point à la fin du montant
    private fun ajouterCaractere(c: String) {
        val montant = binding.tvMontant.text.toString()

        // Un seul point décimal autorisé
        if (c == "." && montant.contains(".")) {
            return
        }

        // Le "0" de départ est remplacé par le premier chiffre
        val nouveauMontant: String
        if (montant == "0" && c != ".") {
            nouveauMontant = c
        } else {
            nouveauMontant = montant + c
        }

        // 10 caractères maximum
        if (nouveauMontant.length > 10) {
            Toast.makeText(this, R.string.msg_longueur_max, Toast.LENGTH_SHORT).show()
            return
        }

        changerMontant(nouveauMontant)
        convertirMontant(false) // bonus : conversion automatique
    }

    // ---------- 6. Conversion ----------

    // Conversion en passant par l'euro (devise pivot)
    private fun convertir(montant: Double, source: String, cible: String): Double {
        // Même devise : le résultat est égal au montant
        if (source == cible) {
            return montant
        }
        val montantEnEuro = montant / taux[source]!!
        return montantEnEuro * taux[cible]!!
    }

    // Lit le montant et les devises puis affiche le résultat
    // afficherErreur = true quand on clique sur "Convertir"
    private fun convertirMontant(afficherErreur: Boolean) {
        val montant = binding.tvMontant.text.toString().toDoubleOrNull() ?: 0.0

        if (montant == 0.0) {
            if (afficherErreur) {
                Toast.makeText(this, R.string.msg_saisir_montant, Toast.LENGTH_SHORT).show()
            }
            changerResultat("")
            return
        }

        val source = binding.spDeviseSource.selectedItem.toString()
        val cible = binding.spDeviseCible.selectedItem.toString()
        val resultat = convertir(montant, source, cible)

        // Arrondi à 2 décimales. Locale.US pour avoir un point (102.56) et pas une virgule
        changerResultat(String.format(Locale.US, "%.2f", resultat))
    }

    // ---------- 7. Inversion des devises ----------
    private fun inverserDevises() {
        val positionSource = binding.spDeviseSource.selectedItemPosition
        val positionCible = binding.spDeviseCible.selectedItemPosition

        binding.spDeviseSource.setSelection(positionCible)
        binding.spDeviseCible.setSelection(positionSource)

        // On relance la conversion seulement si un résultat est déjà affiché
        if (binding.tvResultat.text.isNotEmpty()) {
            convertirMontant(false)
        }
    }

    // ---------- Petites fonctions d'affichage ----------
    private fun changerMontant(valeur: String) {
        binding.tvMontant.text = valeur
        viewModel.montant = valeur
    }

    private fun changerResultat(valeur: String) {
        binding.tvResultat.text = valeur
        viewModel.resultat = valeur
    }
}
