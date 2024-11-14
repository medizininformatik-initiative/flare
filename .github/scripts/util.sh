#!/bin/bash -e

test() {
  if [ "$2" = "$3" ]; then
    echo "OK: the $1 is $3"
  else
    echo "Fail: the $1 is $2, expected $3"
    exit 1
  fi
}

test_array() {
  local name="$1"
  local array1_name="$2"
  local array2_name="$3"

  # Get the length of both arrays
  local len1=$(eval "echo \${#$array1_name[@]}")
  local len2=$(eval "echo \${#$array2_name[@]}")

  # Check if the arrays have the same length
  if [ "$len1" -ne "$len2" ]; then
    echo "Fail: the $name arrays have different lengths ($len1 vs $len2)"
    exit 1
  fi

  # Compare each element
  for i in $(seq 0 $((len1 - 1))); do
    local val1=$(eval "echo \${$array1_name[$i]}")
    local val2=$(eval "echo \${$array2_name[$i]}")
    if [ "$val1" != "$val2" ]; then
      echo "Fail: the $name arrays differ at index $i: $val1 != $val2"
      exit 1
    fi
  done

  echo "OK: the $name arrays are equal"
}
