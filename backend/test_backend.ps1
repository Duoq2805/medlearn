# Disease Draft Flow Test Script
# This script starts the backend, tests the disease draft creation and retrieval, then stops the backend.

# Start backend in the background
Write-Host "Starting backend..."
$backendProcess = Start-Process -FilePath ".\mvnw.cmd" -ArgumentList "spring-boot:run" -PassThru -WindowStyle Hidden

# Wait for backend to be ready (port 6060)
Write-Host "Waiting for backend to start on port 6060..."
$maxWaitSeconds = 60
$startTime = Get-Date
$backendReady = $false
while ((Get-Date) - $startTime -lt (New-TimeSpan -Seconds $maxWaitSeconds)) {
    if (Test-NetConnection -ComputerName localhost -Port 6060 -InformationLevel Quiet) {
        $backendReady = $true
        Write-Host "Backend is ready!"
        break
    }
    Start-Sleep -Milliseconds 500
}

if (-not $backendReady) {
    Write-Host "ERROR: Backend did not start within $maxWaitSeconds seconds."
    Stop-Process -Id $backendProcess.Id
    exit 1
}

# Function to make HTTP requests
function Invoke-ApiRequest {
    param(
        [string]$Method,
        [string]$Uri,
        [hashtable]$Headers = @{},
        [string]$Body = $null,
        [string]$ContentType = "application/json"
    )
    try {
        $response = Invoke-RestMethod -Method $Method -Uri $Uri -Headers $Headers -Body $Body -ContentType $ContentType -ErrorAction Stop
        return $response
    } catch {
        Write-Host "ERROR: Failed to invoke $Method $Uri"
        Write-Host "Exception: $($_.Exception.Message)"
        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode.value__
            Write-Host "Status Code: $statusCode"
            try {
                $errorStream = $_.Exception.Response.GetResponseStream()
                $reader = New-Object System.IO.StreamReader($errorStream)
                $errorContent = $reader.ReadToEnd()
                $reader.Close()
                Write-Host "Response: $errorContent"
            } catch {
                Write-Host "Could not read response content"
            }
        }
        throw
    }
}

# Login function
function Login-User {
    param(
        [string]$Username,
        [string]$Password
    )
    $loginUrl = "http://localhost:6060/api/auth/login"
    $body = @{ username = $Username; password = $Password } | ConvertTo-Json
    try {
        $response = Invoke-ApiRequest -Method Post -Uri $loginUrl -Body $body
        return $response.accessToken
    } catch {
        return $null
    }
}

# Register user function (if needed)
function Register-User {
    param(
        [string]$Username,
        [string]$Password,
        [string]$Email = "$Username@example.com"
    )
    $registerUrl = "http://localhost:6060/api/auth/register"
    $body = @{ 
        username = $Username; 
        password = $Password; 
        email = $Email 
    } | ConvertTo-Json
    try {
        $response = Invoke-ApiRequest -Method Post -Uri $registerUrl -Body $body
        return $response
    } catch {
        return $null
    }
}

# Create disease draft function
function Create-DiseaseDraft {
    param(
        [string]$AccessToken,
        [string]$Name,
        [string]$Slug,
        [long]$CategoryId = $null
    )
    $url = "http://localhost:6060/api/diseases/draft"
    $headers = @{ Authorization = "Bearer $AccessToken" }
    $body = @{ 
        name = $Name; 
        slug = $Slug; 
        categoryId = $CategoryId 
    } | ConvertTo-Json
    try {
        $response = Invoke-ApiRequest -Method Post -Uri $url -Headers $headers -Body $body
        return $response
    } catch {
        return $null
    }
}

# Get disease drafts function
function Get-DiseaseDrafts {
    param(
        [string]$AccessToken,
        [int]$Page = 0,
        [int]$Size = 100
    )
    $url = "http://localhost:6060/api/drafts?page=$Page&size=$Size"
    $headers = @{ Authorization = "Bearer $AccessToken" }
    try {
        $response = Invoke-ApiRequest -Method Get -Uri $url -Headers $headers
        return $response
    } catch {
        return $null
    }
}

# Main test
try {
    Write-Host "=== Starting Disease Draft Flow Test ==="

    # Try to login with default user
    Write-Host "Attempting login with default user (user/user)..."
    $token = Login-User -Username "user" -Password "user"
    if (-not $token) {
        Write-Host "Default user login failed. Attempting to register a new test user..."
        $timestamp = Get-Date -Format "yyyyMMddHHmmss"
        $username = "testuser_$timestamp"
        $password = "TestPass123!"
        $registerResult = Register-User -Username $username -Password $password
        if (-not $registerResult) {
            Write-Host "ERROR: Failed to register new user. Aborting."
            exit 1
        }
        Write-Host "Registered new user: $username"
        $token = Login-User -Username $username -Password $password
        if (-not $token) {
            Write-Host "ERROR: Failed to login with newly registered user. Aborting."
            exit 1
        }
        Write-Host "Logged in with new user."
    } else {
        Write-Host "Logged in with default user."
    }

    # Create a disease draft
    Write-Host "Creating disease draft..."
    $draftName = "Test Disease Draft $(Get-Date -Format 'yyyyMMddHHmmss')"
    $draftSlug = "test-disease-draft-$(Get-Date -Format 'yyyyMMddHHmmss')"
    $createResult = Create-DiseaseDraft -AccessToken $token -Name $draftName -Slug $draftSlug
    if (-not $createResult) {
        Write-Host "ERROR: Failed to create disease draft."
        exit 1
    }
    Write-Host "Disease draft created successfully."
    Write-Host "Response: $($createResult | ConvertTo-Json -Depth 3)"

    # Get disease drafts
    Write-Host "Fetching disease drafts..."
    $draftsResult = Get-DiseaseDrafts -AccessToken $token
    if (-not $draftsResult) {
        Write-Host "ERROR: Failed to fetch disease drafts."
        exit 1
    }
    Write-Host "Drafts response: $($draftsResult | ConvertTo-Json -Depth 3)"

    # Check if our draft is in the list
    if ($draftsResult.content) {
        $matchingDrafts = $draftsResult.content | Where-Object { $_.name -eq $draftName }
        if ($matchingDrafts) {
            Write-Host "SUCCESS: Found draft '$draftName' in the drafts list."
            Write-Host "Total elements: $($draftsResult.totalElements)"
            if ($draftsResult.totalElements -gt 0) {
                Write-Host "TEST PASSED: Disease draft is visible in My Drafts."
                $testPassed = $true
            } else {
                Write-Host "ERROR: totalElements is 0 despite finding draft in content."
                $testPassed = $false
            }
        } else {
            Write-Host "ERROR: Draft '$draftName' not found in the drafts list."
            Write-Host "Available drafts: $($draftsResult.content | Select-Object -ExpandProperty name)"
            $testPassed = $false
        }
    } else {
        Write-Host "ERROR: No content in drafts response."
        $testPassed = $false
    }

    if ($testPassed) {
        Write-Host "=== Test PASSED ==="
        exit 0
    } else {
        Write-Host "=== Test FAILED ==="
        exit 1
    }
} catch {
    Write-Host "ERROR: Test failed with exception: $($_.Exception.Message)"
    exit 1
} finally {
    # Stop backend
    Write-Host "Stopping backend..."
    Stop-Process -Id $backendProcess.Id
}
