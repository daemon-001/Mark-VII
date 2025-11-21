#!/usr/bin/env python3
"""
Firebase Gemini Model Updater Script
Updates Gemini AI models in Firestore gemini_models collection

Usage:
    python update_firebase_gemini_models.py --csv gemini_models.csv

Requirements:
    pip install firebase-admin
"""

import firebase_admin
from firebase_admin import credentials, firestore
import sys
import os
import csv

# ============================================================================
# CONFIGURATION
# ============================================================================

# Path to your Firebase service account key (download from Firebase Console)
SERVICE_ACCOUNT_KEY = "mark-vii-firebase-service-account-key.json"

# Your Firestore collection paths
COLLECTION_PATH = "app_config"
GEMINI_MODELS_DOCUMENT = "gemini_models"

# Default CSV file name
DEFAULT_CSV_FILE = "gemini_models.csv"

# ============================================================================
# FUNCTIONS
# ============================================================================

def initialize_firebase():
    """Initialize Firebase Admin SDK"""
    try:
        if not os.path.exists(SERVICE_ACCOUNT_KEY):
            print(f"❌ Error: Service account key not found!")
            print(f"📥 Download it from:")
            print(f"   Firebase Console → Project Settings → Service Accounts")
            print(f"   → Generate New Private Key")
            print(f"📁 Save it as: {SERVICE_ACCOUNT_KEY}")
            sys.exit(1)
        
        # Check if already initialized
        try:
            firebase_admin.get_app()
            print("✅ Firebase already initialized")
        except ValueError:
            cred = credentials.Certificate(SERVICE_ACCOUNT_KEY)
            firebase_admin.initialize_app(cred)
            print("✅ Firebase initialized successfully")
        
        return firestore.client()
    except Exception as e:
        print(f"❌ Error initializing Firebase: {e}")
        sys.exit(1)

def load_gemini_models_from_csv(csv_file):
    """Load Gemini models from CSV file"""
    try:
        if not os.path.exists(csv_file):
            print(f"❌ Error: CSV file not found: {csv_file}")
            return None
        
        models = []
        with open(csv_file, 'r', encoding='utf-8') as file:
            reader = csv.DictReader(file)
            
            # Check required columns
            required_columns = ['displayName', 'apiModel', 'isAvailable', 'order']
            if not all(col in reader.fieldnames for col in required_columns):
                print(f"❌ Error: CSV must have columns: {', '.join(required_columns)}")
                print(f"   Found columns: {', '.join(reader.fieldnames)}")
                return None
            
            for i, row in enumerate(reader, 1):
                try:
                    # Parse isAvailable (handle various formats)
                    is_available_str = row['isAvailable'].strip().lower()
                    is_available = is_available_str in ['true', 'yes', '1', 'y']
                    
                    # Parse order
                    order = int(row['order'].strip())
                    
                    model = {
                        "displayName": row['displayName'].strip(),
                        "apiModel": row['apiModel'].strip(),
                        "isAvailable": is_available,
                        "order": order
                    }
                    
                    models.append(model)
                except Exception as e:
                    print(f"⚠️ Warning: Error parsing row {i}: {e}")
                    continue
        
        if models:
            print(f"✅ Loaded {len(models)} Gemini models from {csv_file}")
            return models
        else:
            print(f"❌ No valid models found in {csv_file}")
            return None
            
    except Exception as e:
        print(f"❌ Error reading CSV file: {e}")
        return None

def update_gemini_models(db, models):
    """Update Gemini models in Firestore"""
    try:
        # Reference to the gemini_models document
        doc_ref = db.collection(COLLECTION_PATH).document(GEMINI_MODELS_DOCUMENT)
        
        # Prepare the data - Field name must be "list" to match Android app!
        models_data = {
            "list": models,  # Android app expects "list" not "models"
            "lastUpdated": firestore.SERVER_TIMESTAMP
        }
        
        # Update/Create the document
        doc_ref.set(models_data, merge=True)
        
        print(f"\n✅ Successfully updated {len(models)} Gemini models!")
        print(f"📍 Location: {COLLECTION_PATH}/{GEMINI_MODELS_DOCUMENT}")
        print(f"\n📋 Gemini Models:")
        for i, model in enumerate(models, 1):
            status = "✅" if model["isAvailable"] else "❌"
            print(f"   {i:2d}. {status} {model['displayName']:40s} → {model['apiModel']}")
        
        return True
    except Exception as e:
        print(f"❌ Error updating Gemini models: {e}")
        return False

def verify_update(db):
    """Verify the update by reading back the data"""
    try:
        doc_ref = db.collection(COLLECTION_PATH).document(GEMINI_MODELS_DOCUMENT)
        doc = doc_ref.get()
        
        if doc.exists:
            data = doc.to_dict()
            print(f"\n✅ Verification successful!")
            print(f"📊 Total Gemini models in Firestore: {len(data.get('list', []))}")
            return True
        else:
            print(f"\n⚠️ Warning: Could not verify update")
            return False
    except Exception as e:
        print(f"❌ Error verifying update: {e}")
        return False

def list_current_gemini_models(db):
    """List current Gemini models in Firestore"""
    try:
        doc_ref = db.collection(COLLECTION_PATH).document(GEMINI_MODELS_DOCUMENT)
        doc = doc_ref.get()
        
        if doc.exists:
            data = doc.to_dict()
            models = data.get('list', [])
            
            if models:
                print(f"\n📋 Current Gemini Models in Firestore ({len(models)} total):")
                for i, model in enumerate(models, 1):
                    status = "✅" if model.get("isAvailable", True) else "❌"
                    print(f"   {i:2d}. {status} {model.get('displayName', 'N/A'):40s} → {model.get('apiModel', 'N/A')}")
            else:
                print("\n📋 No Gemini models found in Firestore")
        else:
            print("\n📋 Gemini models document does not exist yet")
    except Exception as e:
        print(f"❌ Error listing Gemini models: {e}")

def create_sample_csv():
    """Create a sample CSV file with Gemini models"""
    try:
        sample_file = "gemini_models_sample.csv"
        with open(sample_file, 'w', newline='', encoding='utf-8') as file:
            writer = csv.writer(file)
            writer.writerow(['displayName', 'apiModel', 'isAvailable', 'order'])
            writer.writerow(['Gemini 2.0 Flash (Experimental)', 'gemini-2.0-flash-exp', 'TRUE', '1'])
            writer.writerow(['Gemini 1.5 Flash', 'gemini-1.5-flash', 'TRUE', '2'])
            writer.writerow(['Gemini 1.5 Flash-8B', 'gemini-1.5-flash-8b', 'TRUE', '3'])
            writer.writerow(['Gemini 1.5 Pro', 'gemini-1.5-pro', 'TRUE', '4'])
            writer.writerow(['Gemini 1.0 Pro', 'gemini-1.0-pro', 'TRUE', '5'])
        
        print(f"✅ Sample CSV created: {sample_file}")
        print(f"📝 Edit this file and run:")
        print(f"   python update_firebase_gemini_models.py --csv {sample_file}")
    except Exception as e:
        print(f"❌ Error creating sample CSV: {e}")

# ============================================================================
# MAIN
# ============================================================================

def main():
    """Main function"""
    
    print("\n" + "="*60)
    print("🔥 Firebase Gemini Model Updater for Mark VII")
    print("="*60)
    
    # Check command line arguments
    if len(sys.argv) > 1:
        if sys.argv[1] == "--help" or sys.argv[1] == "-h":
            print("\nUsage:")
            print("  python update_firebase_gemini_models.py --csv gemini_models.csv   # Update from CSV")
            print("  python update_firebase_gemini_models.py --list                    # List current models")
            print("  python update_firebase_gemini_models.py --sample                  # Create sample CSV")
            print("  python update_firebase_gemini_models.py --help                    # Show this help")
            print("\nCSV Format:")
            print("  displayName,apiModel,isAvailable,order")
            print("\nExample:")
            print("  Gemini 2.0 Flash,gemini-2.0-flash-exp,TRUE,1")
            print("  Gemini 1.5 Flash,gemini-1.5-flash,TRUE,2")
            sys.exit(0)
    
    # Initialize Firebase
    db = initialize_firebase()
    
    # Handle command line arguments
    if len(sys.argv) > 1:
        if sys.argv[1] == "--csv":
            if len(sys.argv) < 3:
                csv_file = DEFAULT_CSV_FILE
            else:
                csv_file = sys.argv[2]
            
            models = load_gemini_models_from_csv(csv_file)
            if models:
                print(f"\n📋 Preview of Gemini Models:")
                for i, model in enumerate(models, 1):
                    status = "✅" if model["isAvailable"] else "❌"
                    print(f"   {i:2d}. {status} {model['displayName']:40s} → {model['apiModel']}")
                
                print(f"\n⚠️ This will upload {len(models)} Gemini models to Firestore")
                confirm = input("Continue? (y/n): ").strip().lower()
                if confirm == 'y':
                    if update_gemini_models(db, models):
                        verify_update(db)
                else:
                    print("❌ Cancelled")
        elif sys.argv[1] == "--list":
            list_current_gemini_models(db)
        elif sys.argv[1] == "--sample":
            create_sample_csv()
        else:
            print(f"❌ Unknown argument: {sys.argv[1]}")
            print("Use --help for usage information")
    else:
        # Default: use gemini_models.csv
        csv_file = DEFAULT_CSV_FILE
        
        if not os.path.exists(csv_file):
            print(f"\n❌ Default CSV file not found: {csv_file}")
            print(f"\n💡 Create it using: python update_firebase_gemini_models.py --sample")
            print(f"   Or specify a file: python update_firebase_gemini_models.py --csv your_file.csv")
            sys.exit(1)
        
        print(f"\n📤 Using default CSV file: {csv_file}")
        
        models = load_gemini_models_from_csv(csv_file)
        if models:
            print(f"\n📋 Preview of Gemini Models:")
            for i, model in enumerate(models, 1):
                status = "✅" if model["isAvailable"] else "❌"
                print(f"   {i:2d}. {status} {model['displayName']:40s} → {model['apiModel']}")
            
            print(f"\n⚠️ This will upload {len(models)} Gemini models to Firestore")
            confirm = input("Continue? (y/n): ").strip().lower()
            if confirm == 'y':
                if update_gemini_models(db, models):
                    verify_update(db)
            else:
                print("❌ Cancelled")

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\n👋 Interrupted by user. Goodbye!")
        sys.exit(0)
    except Exception as e:
        print(f"\n❌ Unexpected error: {e}")
        sys.exit(1)
