// =====================================================
// CONFIGURATION DE L'API
// =====================================================
// URL de base de l'API (à adapter selon votre environnement)
const API_URL = 'http://localhost:8080/api/auth';

// =====================================================
// RÉCUPÉRATION DES ÉLÉMENTS DU DOM
// =====================================================
// On récupère le formulaire et tous les champs
const form = document.getElementById('registerForm');
const submitBtn = document.getElementById('submitBtn');
const errorAlert = document.getElementById('errorAlert');
const errorMessage = document.getElementById('errorMessage');

// Récupération de tous les champs du formulaire
const nomInput = document.getElementById('nom');
const prenomInput = document.getElementById('prenom');
const telephoneInput = document.getElementById('telephone');
const dateNaissanceInput = document.getElementById('dateNaissance');
const adressePhysiqueInput = document.getElementById('adressePhysique');
const codePostalInput = document.getElementById('codePostal');
const villeInput = document.getElementById('ville');
const emailInput = document.getElementById('email');
const motDePasseInput = document.getElementById('motDePasse');
const confirmMotDePasseInput = document.getElementById('confirmMotDePasse');
const acceptTermsInput = document.getElementById('acceptTerms');

// =====================================================
// FONCTION : AFFICHER UN MESSAGE D'ERREUR GÉNÉRAL
// =====================================================
function showGeneralError(message) {
  errorMessage.textContent = message;
  errorAlert.classList.add('show');
  // Faire défiler vers le haut pour voir l'erreur
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

// =====================================================
// FONCTION : CACHER LE MESSAGE D'ERREUR GÉNÉRAL
// =====================================================
function hideGeneralError() {
  errorAlert.classList.remove('show');
}

// =====================================================
// FONCTION : AFFICHER/MASQUER UNE ERREUR DE CHAMP
// =====================================================
function showFieldError(fieldId, message) {
  const errorDiv = document.getElementById(fieldId + 'Error');
  const errorText = document.getElementById(fieldId + 'ErrorText');
  const inputField = document.getElementById(fieldId);

  if (errorDiv) {
    errorDiv.classList.add('show');
    if (errorText && message) {
      errorText.textContent = message;
    }
  }

  // Ajouter la classe d'erreur au champ
  if (inputField) {
    inputField.classList.add('error');
  }
}

function hideFieldError(fieldId) {
  const errorDiv = document.getElementById(fieldId + 'Error');
  const inputField = document.getElementById(fieldId);

  if (errorDiv) {
    errorDiv.classList.remove('show');
  }

  // Retirer la classe d'erreur
  if (inputField) {
    inputField.classList.remove('error');
  }
}

// =====================================================
// FONCTION : VALIDER L'EMAIL
// =====================================================
function isValidEmail(email) {
  // Expression régulière simple pour valider un email
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

// =====================================================
// FONCTION : VALIDER TOUS LES CHAMPS
// =====================================================
function validateForm() {
  let isValid = true;

  // Cacher toutes les erreurs avant de revalider
  hideGeneralError();
  hideGeneralError();
  hideFieldError('nom');
  hideFieldError('prenom');
  hideFieldError('telephone');
  hideFieldError('dateNaissance');
  hideFieldError('adressePhysique');
  hideFieldError('codePostal');
  hideFieldError('ville');
  hideFieldError('email');
  hideFieldError('motDePasse');
  hideFieldError('confirmMotDePasse');
  hideFieldError('acceptTerms');

  // Validation du nom
  if (!nomInput.value.trim()) {
    showFieldError('nom');
    isValid = false;
  }

  // Validation du prénom
  if (!prenomInput.value.trim()) {
    showFieldError('prenom');
    isValid = false;
  }

  // Validation du téléphone
  if (!telephoneInput.value.trim()) {
    showFieldError('telephone');
    isValid = false;
  }

  // Validation de la date de naissance
  if (!dateNaissanceInput.value) {
    showFieldError('dateNaissance');
    isValid = false;
  }

  // Validation de l'adresse
  if (!adressePhysiqueInput.value.trim()) {
    showFieldError('adressePhysique');
    isValid = false;
  }

  // Validation du code postal
  if (!codePostalInput.value.trim()) {
    showFieldError('codePostal');
    isValid = false;
  }

  // Validation de la ville
  if (!villeInput.value.trim()) {
    showFieldError('ville');
    isValid = false;
  }

  // Validation de l'email
  if (!emailInput.value.trim()) {
    showFieldError('email', 'L\'email est requis');
    isValid = false;
  } else if (!isValidEmail(emailInput.value)) {
    showFieldError('email', 'Email invalide');
    isValid = false;
  }



  // Validation du mot de passe
  if (!motDePasseInput.value) {
    showFieldError('motDePasse', 'Le mot de passe est requis');
    isValid = false;
  } else if (motDePasseInput.value.length < 6) {
    showFieldError('motDePasse', 'Min. 6 caractères');
    isValid = false;
  }

  // Validation de la confirmation du mot de passe
  if (!confirmMotDePasseInput.value) {
    showFieldError('confirmMotDePasse', 'Confirmation requise');
    isValid = false;
  } else if (motDePasseInput.value !== confirmMotDePasseInput.value) {
    showFieldError('confirmMotDePasse', 'Mots de passe différents');
    isValid = false;
  }

  // Validation de l'acceptation des conditions
  if (!acceptTermsInput.checked) {
    showFieldError('acceptTerms');
    isValid = false;
  }

  return isValid;
}

// =====================================================
// FONCTION : ACTIVER/DÉSACTIVER LE BOUTON DE SOUMISSION
// =====================================================
function setLoading(isLoading) {
  if (isLoading) {
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner"></span><span>Inscription...</span>';
  } else {
    submitBtn.disabled = false;
    submitBtn.innerHTML = '<i class="bi bi-person-plus"></i><span>S\'inscrire</span>';
  }
}

// =====================================================
// FONCTION : ENVOYER LES DONNÉES À L'API
// =====================================================
async function submitRegistration(userData) {
  try {
    // Préparer les données pour l'API (format attendu par le backend)
    const registerData = {
      utilisateur: {
        nom: userData.nom,
        prenom: userData.prenom,
        telephone: userData.telephone,
        email: userData.email,
        dateNaissance: userData.dateNaissance,
        adressePhysique: userData.adressePhysique,
        codePostal: userData.codePostal,
        ville: userData.ville,
        role: 'client',
        iban: '',
        medias: ''
      },
      motDePasse: userData.motDePasse
    };

    // Faire l'appel API avec fetch
    const response = await fetch(`${API_URL}/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(registerData)
    });

    // Récupérer la réponse JSON
    const data = await response.json();

    // Vérifier si l'inscription a réussi
    if (data.result === 'SUCCESS') {
      // Rediriger vers la page de vérification d'email
      window.location.href = `/auth/verify-email?email=${encodeURIComponent(userData.email)}`;
    } else {
      // Afficher le message d'erreur retourné par l'API
      showGeneralError(data.message || 'Une erreur inconnue est survenue.');
      setLoading(false);
    }
  } catch (error) {
    // Erreur réseau ou autre erreur

    showGeneralError('Une erreur est survenue lors de l\'inscription. Veuillez réessayer.');
    setLoading(false);
  }
}

// =====================================================
// GESTIONNAIRE D'ÉVÉNEMENT : SOUMISSION DU FORMULAIRE
// =====================================================
form.addEventListener('submit', function (event) {
  // Empêcher le rechargement de la page
  event.preventDefault();

  // Valider le formulaire
  if (!validateForm()) {
    return; // Arrêter si la validation échoue
  }

  // Activer l'état de chargement
  setLoading(true);

  // Récupérer les valeurs du formulaire
  const userData = {
    nom: nomInput.value.trim(),
    prenom: prenomInput.value.trim(),
    telephone: telephoneInput.value.trim(),
    dateNaissance: dateNaissanceInput.value,
    adressePhysique: adressePhysiqueInput.value.trim(),
    codePostal: codePostalInput.value.trim(),
    ville: villeInput.value.trim(),
    email: emailInput.value.trim(),
    motDePasse: motDePasseInput.value
  };

  // Envoyer les données à l'API
  submitRegistration(userData);
});

// =====================================================
// VALIDATION EN TEMPS RÉEL (OPTIONNEL)
// =====================================================
// Masquer les erreurs quand l'utilisateur modifie un champ
nomInput.addEventListener('input', () => hideFieldError('nom'));
prenomInput.addEventListener('input', () => hideFieldError('prenom'));
telephoneInput.addEventListener('input', () => hideFieldError('telephone'));
dateNaissanceInput.addEventListener('input', () => hideFieldError('dateNaissance'));
adressePhysiqueInput.addEventListener('input', () => hideFieldError('adressePhysique'));
codePostalInput.addEventListener('input', () => hideFieldError('codePostal'));
villeInput.addEventListener('input', () => hideFieldError('ville'));
emailInput.addEventListener('input', () => hideFieldError('email'));
motDePasseInput.addEventListener('input', () => hideFieldError('motDePasse'));
confirmMotDePasseInput.addEventListener('input', () => hideFieldError('confirmMotDePasse'));
acceptTermsInput.addEventListener('change', () => hideFieldError('acceptTerms'));
