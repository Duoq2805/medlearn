# Check the database for disease_draft records and the user id for duongkt.st.2005@gmail.com
# This script does not require the backend to be running.

# Connection details from .env
$host = "localhost"
$port = "5436"
$database = "medlearn_db"
$username = "medlearn"
$password = "Medlearn@123"  # from .env: SPRING_DATASOURCE_PASSWORD=Medlearn@123

# Find psql executable
$psqlPath = "C:\Program Files\PostgreSQL\15\bin\psql.exe"
if (-not (Test-Path $psqlPath)) {
    # Try to find psql in PATH
    $psqlCmd = Get-Command psql -ErrorAction SilentlyContinue
    if ($psqlCmd) {
        $psqlPath = $psqlCmd.Source
    } else {
        Write-Host "ERROR: psql not found. Please install PostgreSQL and add psql to PATH, or update the path in the script."
        exit 1
    }
}

# Function to run a SQL query
function Run-SqlQuery {
    param(
        [string]$Query
    )
    $connString = "host=$host port=$port dbname=$database user=$username password=$password"
    try {
        $result = & $psqlPath $connString -t -c "$query"
        return $result
    } catch {
        Write-Host "ERROR: Failed to run SQL query: $($_.Exception.Message)"
        return $null
    }
}

Write-Host "=== Checking user ID for duongkt.st.2005@gmail.com ==="
$email = 'duongkt.st.2005@gmail.com'
$userQuery = "SELECT id FROM ""user"" WHERE email = '$email';"
$userResult = Run-SqlQuery -Query $userQuery
if ($null -eq $userResult) {
    Write-Host "Failed to retrieve user ID."
    exit 1
}
$userResult = $userResult.Trim()
if (-not $userResult) {
    Write-Host "User with email '$email' not found."
    exit 1
}
$userId = [int64]$userResult
Write-Host "User ID: $userId"

Write-Host "`n=== Checking disease_draft records ==="
$draftQuery = "SELECT id, disease_id, title, status, created_by, created_at FROM disease_draft ORDER BY created_at DESC;"
$draftResult = Run-SqlQuery -Query $draftQuery
if ($null -eq $draftResult) {
    Write-Host "Failed to retrieve disease_draft records."
    exit 1
}
if (-not $draftResult) {
    Write-Host "No disease_draft records found in the database."
} else {
    Write-Host "Disease_draft records (raw output):"
    Write-Host $draftResult
    # Now let's get the same data in CSV format for easier parsing
    $draftCsvQuery = "SELECT id, disease_id, title, status, created_by, created_at FROM disease_draft ORDER BY created_at DESC;"
    $draftCsvResult = Run-SqlQuery -Query $draftCsvQuery
    if ($null -eq $draftCsvResult) {
        Write-Host "Failed to retrieve disease_draft records in CSV format."
    } else {
        Write-Host "`nParsing disease_draft records (CSV format):"
        Write-Host $draftCsvResult
        $csvLines = $draftCsvResult -split "`n"
        foreach ($csvLine in $csvLines) {
            if ($csvLine.Trim() -eq "") { continue }
            $fields = $csvLine -split ','
            if ($fields.Length -lt 6) { continue }
            $id = $fields[0].Trim()
            $diseaseId = $fields[1].Trim()
            $title = $fields[2].Trim()
            $status = $fields[3].Trim()
            $createdBy = $fields[4].Trim()
            $createdAt = $fields[5].Trim()
            Write-Host "Record: id=$id, disease_id=$diseaseId, title='$title', status=$status, created_by=$createdBy, created_at=$createdAt"
            if ($createdBy -eq $userId.ToString()) {
                Write-Host "  --> created_by MATCHES the user ID ($userId)"
            } else {
                Write-Host "  --> created_by does NOT match the user ID ($userId)"
            }
        }
    }
}

Write-Host "`n=== Check completed ==="
exit 0
