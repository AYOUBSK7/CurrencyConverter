package com.example.currencyconverter

import androidx.lifecycle.ViewModel

// Bonus : le ViewModel garde les valeurs quand l'activité est recréée
// (rotation, mode sombre...). L'activité est détruite mais pas le ViewModel.
class ConverterViewModel : ViewModel() {
    var montant = "0"
    var resultat = ""
    var positionSource = -1   // -1 = pas encore choisi
    var positionCible = -1
}
