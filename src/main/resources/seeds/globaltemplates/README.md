# Global Templates Seed Files

This directory contains JSON seed files for global templates that can be applied to organizations.

## Entity Type Naming Convention

Use **Java class names** (PascalCase) for entity types in JSON files. The system will automatically normalize variations, but using the correct names is recommended.

### Supported Entity Types

| JSON Entity Type | Java Entity Class | Description |
|-----------------|-------------------|-------------|
| `Brand` | `Brand` | Product brands (global, not org-specific) |
| `Category` | `Category` | Product categories (org-specific) |
| `UnitOfMeasure` | `UnitOfMeasure` | Units of measure (org-specific) |
| `ProductTemplate` | `ProductTemplate` | Product templates (org-specific) |
| `TaxRule` | `TaxRule` | Tax rules (org-specific) |

### Legacy/Alternative Names (Auto-Normalized)

The following variations are automatically normalized to the correct entity type:
- `UOM`, `UNIT_OF_MEASURE`, `UnitOfMeasure` → `UnitOfMeasure`
- `PRODUCT_TEMPLATE`, `ProductTemplate` → `ProductTemplate`
- `TAX_RULE`, `TaxRule` → `TaxRule`
- `BRAND`, `Brand` → `Brand`
- `CATEGORY`, `Category` → `Category`

## Field Name Normalization

**Important**: Field names in JSON files can be written in either **snake_case** or **camelCase**. The `TemplateSeeder` automatically normalizes all field names to **camelCase** before storing them in the database.

### Examples of Normalization:
- `country_code` → `countryCode`
- `tax_rate` → `taxRate`
- `category_code` → `categoryCode`
- `unit_of_measure_code` → `unitOfMeasureCode`
- `cost_price` → `costPrice`
- `retail_price` → `retailPrice`
- `brand_name` → `brandName`
- `parent_code` → `parentCode`

This means you can write JSON files using either naming convention, and they will be normalized automatically.

## JSON File Format

```json
{
  "template": {
    "name": "Template Name",
    "code": "TEMPLATE_CODE",
    "type": "TEMPLATE_TYPE",
    "version": 1,
    "isActive": true
  },
  "items": [
    {
      "entity_type": "Brand",
      "data": {
        "name": "Brand Name",
        "description": "Optional description"
      }
    },
    {
      "entity_type": "Category",
      "data": {
        "code": "CAT_CODE",
        "name": "Category Name",
        "description": "Optional description",
        "parent_code": "PARENT_CODE" // Optional (normalized to parentCode)
      }
    },
    {
      "entity_type": "UnitOfMeasure",
      "data": {
        "code": "UOM_CODE",
        "name": "Unit Name",
        "description": "Optional description"
      }
    },
    {
      "entity_type": "ProductTemplate",
      "data": {
        "name": "Product Name",
        "description": "Product description",
        "categoryCode": "CAT_CODE", // Reference to category by code
        "brandName": "Brand Name", // Reference to brand by name
        "unitOfMeasureCode": "UOM_CODE", // Reference to UOM by code
        "imageUrl": "https://...",
        "reorderPoint": 10,
        "requiresExpiry": false,
        "isActive": true,
        // Optional: If SKU is provided, a ProductVariant will be created
        "sku": "PROD-SKU-001",
        "barcode": "1234567890123",
        "costPrice": "10.00",
        "retailPrice": "15.00"
      }
    },
    {
      "entity_type": "TaxRule",
      "data": {
        "countryCode": "US",
        "taxRate": "8.5",
        "description": "Sales tax"
      }
    }
  ]
}
```

## Data Field Formats

### Brand
- **Required**: `name`
- **Optional**: `description`, `logoUrl`, `website`

### Category
- **Required**: `code`, `name`
- **Optional**: `description`, `isActive` (default: true), `parent_code` (references another category by code)

### UnitOfMeasure
- **Required**: `code`, `name`
- **Optional**: `description`

### ProductTemplate
- **Required**: `name`, `unitOfMeasureCode` (or `unitOfMeasureId`)
- **Optional**: 
  - `description`
  - `categoryCode` or `categoryId` (reference to category)
  - `brandName` or `brandId` (reference to brand)
  - `imageUrl`
  - `reorderPoint` (integer)
  - `requiresExpiry` (boolean, default: false)
  - `isActive` (boolean, default: true)
  - **ProductVariant fields** (if SKU is provided, a ProductVariant will be automatically created):
    - `sku` (string, required if creating variant)
    - `barcode` (string, optional)
    - `costPrice` (decimal/string, required if creating variant)
    - `retailPrice` (decimal/string, required if creating variant)
    - `attributeValues` (object/map, optional)
    - `isActive` (boolean, default: true)

### TaxRule
- **Required**: `countryCode`, `taxRate` (as string, e.g., "8.5")
- **Optional**: `description`

## Important Notes

1. **Entity References**: When referencing other entities in `ProductTemplate`:
   - Use `categoryCode` to reference a category by code (must be created before the product)
   - Use `brandName` to reference a brand by name
   - Use `unitOfMeasureCode` or `uomCode` to reference a unit of measure by code

2. **Order Matters**: Items are processed in the order they appear in the JSON array. Make sure dependent entities (like categories, brands, UOMs) are defined before entities that reference them (like ProductTemplate).

3. **Organization Context**: All entities (except Brand) are organization-specific. When a template is applied, entities are created for the specific organization.

4. **Duplicate Handling**: The system automatically skips entities that already exist based on:
   - Brand: by name (global)
   - Category: by code + organization
   - UnitOfMeasure: by code + organization
   - ProductTemplate: by name + organization
   - TaxRule: by countryCode + organization

