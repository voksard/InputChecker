# Partage d'éléments InputChecker

## Comment partager vos éléments avec d'autres joueurs

### Méthode 1 : Export/Import avec commandes (Recommandé)

#### Exporter un élément
```
/inputchecker export <nom_element>
```
Exemple :
```
/inputchecker export My Speedrun Element
```

- Cela créera un fichier dans `minecraft/config/` avec le nom de l'élément
- Par exemple : `My_Speedrun_Element.json`
- Vous pouvez ensuite envoyer ce fichier à vos amis

#### Importer un élément
```
/inputchecker import <nom_fichier>
```
Exemple :
```
/inputchecker import My_Speedrun_Element.json
```

- Placez d'abord le fichier JSON reçu dans `minecraft/config/`
- Puis utilisez la commande pour l'importer
- L'élément apparaîtra dans votre catalogue (touche G)
- Si un élément avec le même nom existe déjà, un suffixe sera ajouté automatiquement (ex: "Element (1)")

### Méthode 2 : Partage du fichier complet

Vous pouvez également partager votre fichier `inputchecker.json` complet qui contient tous vos éléments.

**Emplacement du fichier :**
```
minecraft/config/inputchecker.json
```

**⚠️ Attention :** Cette méthode remplacera TOUS les éléments existants du destinataire.

### Bonnes pratiques

1. **Pour partager un seul élément :** Utilisez `/inputchecker export`
2. **Pour partager plusieurs éléments :** Exportez-les un par un
3. **Pour backup complet :** Sauvegardez le fichier `inputchecker.json`

### Format du fichier JSON

Les fichiers exportés contiennent :
- Le nom de l'élément
- Tous les inputs par tick
- Les checkboxes (jmp, spr, snk, nj, ns, nk)
- L'ID unique (régénéré automatiquement lors de l'import)

### Exemple de contenu d'un fichier exporté

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "45 Strafe",
  "tickInputs": ["w+d", "w+prs-a", "w+prs-a", "w+d"],
  "checkSprint": [false, false, false, false],
  "checkJump": [false, false, false, false],
  "checkSneak": [false, false, false, false],
  "noSprint": [false, false, false, false],
  "noJump": [false, false, false, false],
  "noSneak": [false, false, false, false]
}
```

