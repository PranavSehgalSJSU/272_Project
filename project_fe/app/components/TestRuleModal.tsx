'use client';

import React, { useState } from 'react';
import { Rule } from '../page';

type TestRuleModalProps = {
  rule: Rule | null;
  isOpen: boolean;
  onClose: () => void;
};

export function TestRuleModal({ rule, isOpen, onClose }: TestRuleModalProps) {
  const [testing, setTesting] = useState(false);
  const [testResult, setTestResult] = useState<any>(null);
  const [testData, setTestData] = useState('');

  const getTestDataTemplate = () => {
    if (rule?.source === 'WEATHER') {
      return JSON.stringify({
        temp_c: 19.0,
        temp_f: 66.2,
        humidity: 65,
        city: "San Jose",
        description: "Cold weather",
        condition: "Cold"
      }, null, 2);
    } else if (rule?.source === 'STATUS') {
      return JSON.stringify({
        status: "down",
        response_time: 5000,
        url: "https://example.com"
      }, null, 2);
    }
    return JSON.stringify({
      value: 70
    }, null, 2);
  };

  // Initialize test data when modal opens
  React.useEffect(() => {
    if (isOpen && rule && !testData) {
      setTestData(getTestDataTemplate());
    }
  }, [isOpen, rule]);

  const handleTest = async () => {
    if (!rule?.id) return;

    let parsedData;
    try {
      parsedData = JSON.parse(testData);
    } catch (error) {
      setTestResult({ error: 'Invalid JSON data. Please check your test data format.' });
      return;
    }

    try {
      setTesting(true);
      const payload = {
        mockData: parsedData,
        send: true  // This tells the backend to actually send emails to all users
      };

      const response = await fetch(`http://localhost:8080/api/rules/${rule.id}/test`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload)
      });
      
      if (response.ok) {
        const result = await response.json();
        setTestResult(result);
      } else {
        const errorText = await response.text();
        setTestResult({ error: `Test failed: ${errorText}` });
      }
    } catch (error) {
      setTestResult({ error: 'Test failed: ' + error });
    } finally {
      setTesting(false);
    }
  };

  console.log('TestRuleModal render:', { isOpen, rule: rule?.name });

  if (!isOpen || !rule) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" style={{zIndex: 9999}}>
      <div className="bg-white rounded-lg p-6 w-full max-w-2xl shadow-xl max-h-[90vh] overflow-y-auto">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-lg font-medium">Test Rule: {rule.name}</h2>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-700 text-xl"
          >
            ×
          </button>
        </div>
        
        <div className="space-y-4">
          {/* Rule Info */}
          <div className="bg-gray-50 p-3 rounded">
            <div className="text-sm">
              <div><strong>Source:</strong> {rule.source}</div>
              <div><strong>Condition:</strong> <code className="bg-gray-200 px-1 rounded">{rule.condition}</code></div>
              <div><strong>Message:</strong> {rule.message?.header}</div>
              <div><strong>Channels:</strong> {rule.message?.channels?.join(', ')}</div>
            </div>
          </div>

          {/* JSON Test Data Input */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Test JSON Data
            </label>
            <textarea
              value={testData}
              onChange={(e) => setTestData(e.target.value)}
              className="w-full h-48 border border-gray-300 rounded p-3 font-mono text-sm"
              placeholder="Enter JSON test data..."
            />
            <p className="text-xs text-gray-500 mt-1">
              Modify the JSON above to test different conditions. This data will be used to evaluate the rule.
            </p>
          </div>

          <div className="bg-orange-50 p-3 rounded border-l-4 border-orange-400">
            <div className="text-sm text-orange-800">
              <strong>⚠️ This will send real emails/SMS to all users in the database!</strong>
            </div>
          </div>

          {/* Test Result */}
          {testResult && (
            <div className="border rounded-lg p-3 bg-gray-50">
              <h3 className="font-medium text-sm mb-2">Test Result:</h3>
              <pre className="text-xs overflow-x-auto whitespace-pre-wrap">
                {JSON.stringify(testResult, null, 2)}
              </pre>
            </div>
          )}

          {/* Action Buttons */}
          <div className="flex gap-2 pt-4 border-t">
            <button
              onClick={handleTest}
              disabled={testing}
              className="flex-1 bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600 disabled:opacity-50"
            >
              {testing ? 'Testing & Sending...' : 'Run Test & Send to All Users'}
            </button>
            <button
              onClick={() => setTestData(getTestDataTemplate())}
              className="px-4 py-2 bg-gray-100 text-gray-700 rounded hover:bg-gray-200"
            >
              Reset Template
            </button>
            <button
              onClick={onClose}
              className="px-4 py-2 bg-gray-300 text-gray-700 rounded hover:bg-gray-400"
            >
              Close
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}